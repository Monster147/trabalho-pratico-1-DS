package pt.lisboapedala.estacao.disponibilidade.internal;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import pt.lisboapedala.estacao.disponibilidade.api.Disponibilidade;
import pt.lisboapedala.estacao.disponibilidade.api.DisponibilidadeEstacao;
import pt.lisboapedala.estacao.ocupacao.api.Ocupacao;
import pt.lisboapedala.estacao.ocupacao.api.OcupacaoAtualizada;
import pt.lisboapedala.estacao.ocupacao.api.OcupacaoEstacao;
import pt.lisboapedala.frota.manutencao.api.ManutencaoApi;
import pt.lisboapedala.frota.telemetria.api.EstadoBicicletaReportado;

/**
 * Mantém a vista de leitura do mapa (C2):
 * <ul>
 *   <li>OcupacaoAtualizada → recalcula só essa estação;</li>
 *   <li>telemetria (~35 msg/s) → só atualiza a bateria em memória, sem recálculo;</li>
 *   <li>reconstrução completa no arranque e periodicamente (fora de serviço e bateria).</li>
 * </ul>
 * As consultas ({@link #mapa()}, {@link #daEstacao}) só leem a cache.
 */
@Service
class DisponibilidadeService implements Disponibilidade {

    private static final Logger LOG = LoggerFactory.getLogger(DisponibilidadeService.class);

    private final Ocupacao ocupacao;
    private final Optional<ManutencaoApi> manutencao;
    private final VistaMapaCache cache;
    private final int limiarBateriaBaixa;

    DisponibilidadeService(Ocupacao ocupacao, Optional<ManutencaoApi> manutencao, VistaMapaCache cache,
                           @Value("${lisboapedala.disponibilidade.limiar-bateria-baixa:20}") int limiarBateriaBaixa) {
        this.ocupacao = ocupacao;
        this.manutencao = manutencao;
        this.cache = cache;
        this.limiarBateriaBaixa = limiarBateriaBaixa;
    }

    @Override
    public List<DisponibilidadeEstacao> mapa() {
        return cache.todas().stream().sorted(Comparator.comparing(DisponibilidadeEstacao::estacaoId)).toList();
    }

    @Override
    public Optional<DisponibilidadeEstacao> daEstacao(String estacaoId) {
        return cache.daEstacao(estacaoId);
    }

    @EventListener
    public void aoAtualizarOcupacao(OcupacaoAtualizada evento) {
        Set<String> foraDeServico = foraDeServico();
        ocupacao.daEstacao(evento.estacaoId()).ifPresent(o -> cache.guardar(calcular(o, foraDeServico)));
    }

    @EventListener
    public void aoReceberTelemetria(EstadoBicicletaReportado evento) {
        cache.registarBateria(evento.bicicletaId(), evento.bateriaPercentagem());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoArrancar() {
        reconstruir();
    }

    @Scheduled(initialDelayString = "${lisboapedala.disponibilidade.reconstrucao-ms:60000}",
               fixedDelayString = "${lisboapedala.disponibilidade.reconstrucao-ms:60000}")
    public void reconstruir() {
        try {
            Set<String> foraDeServico = foraDeServico();
            List<OcupacaoEstacao> todas = ocupacao.todas();
            todas.forEach(o -> cache.guardar(calcular(o, foraDeServico)));
            cache.manterApenas(todas.stream().map(OcupacaoEstacao::estacaoId).collect(Collectors.toSet()));
        } catch (RuntimeException e) {
            LOG.warn("Reconstrução da vista do mapa falhou; mantém-se a versão anterior", e);
        }
    }

    private Set<String> foraDeServico() {
        return manutencao.map(ManutencaoApi::bicicletasForaDeServico).orElse(Set.of());
    }

    private DisponibilidadeEstacao calcular(OcupacaoEstacao o, Set<String> foraDeServico) {
        // Bicicletas encaixadas ainda sem identificação conhecida contam como disponíveis.
        int avariadas = (int) o.bicicletasConhecidas().stream().filter(foraDeServico::contains).count();
        int bateriaBaixa = (int) o.bicicletasConhecidas().stream()
                .filter(b -> !foraDeServico.contains(b))
                .filter(b -> cache.bateria(b).map(p -> p < limiarBateriaBaixa).orElse(false))
                .count();
        return new DisponibilidadeEstacao(o.estacaoId(), o.nome(), o.latitude(), o.longitude(),
                Math.max(0, o.docasOcupadas() - avariadas), bateriaBaixa, o.docasLivres(), Instant.now());
    }
}
