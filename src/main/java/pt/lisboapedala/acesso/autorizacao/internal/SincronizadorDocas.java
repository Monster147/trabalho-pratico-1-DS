package pt.lisboapedala.acesso.autorizacao.internal;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pt.lisboapedala.acesso.identificacao.api.CartaoNaveganteElegivel;
import pt.lisboapedala.acesso.identificacao.api.ElegibilidadeProvider;
import pt.lisboapedala.estacao.docas.api.PacoteAutorizacao;
import pt.lisboapedala.estacao.docas.api.SincronizacaoDocas;
import pt.lisboapedala.frota.manutencao.api.ManutencaoApi;

/**
 * Sincronização assíncrona com as docas físicas (K2, ADR-001).
 * Corre em background, fora de qualquer pedido do utilizador, e prepara tudo o que a doca precisa
 * para decidir o desbloqueio sozinha: chave pública dos QR, cartões Navegante elegíveis e
 * bicicletas fora de serviço. Só gera nova versão quando o conteúdo muda.
 * Se alguma dependência falhar, as docas continuam com o último pacote válido.
 */
@Component
class SincronizadorDocas {

    private static final Logger LOG = LoggerFactory.getLogger(SincronizadorDocas.class);

    private final ElegibilidadeProvider elegibilidade;
    private final Optional<ManutencaoApi> manutencao;
    private final SincronizacaoDocas docas;
    private final ChavesAssinaturaQr chaves;
    private final AtomicLong versao = new AtomicLong(System.currentTimeMillis());

    private volatile ConteudoPacote ultimoConteudo;

    SincronizadorDocas(ElegibilidadeProvider elegibilidade, Optional<ManutencaoApi> manutencao,
                       SincronizacaoDocas docas, ChavesAssinaturaQr chaves) {
        this.elegibilidade = elegibilidade;
        this.manutencao = manutencao;
        this.docas = docas;
        this.chaves = chaves;
    }

    @Scheduled(initialDelayString = "${lisboapedala.autorizacao.sincronizacao.atraso-inicial-ms:1000}",
               fixedDelayString = "${lisboapedala.autorizacao.sincronizacao.intervalo-ms:30000}")
    public void sincronizar() {
        try {
            var conteudo = new ConteudoPacote(
                    chaves.chavePublicaBase64(),
                    elegibilidade.cartoesNaveganteElegiveis().stream()
                            .map(CartaoNaveganteElegivel::numeroCartao)
                            .collect(Collectors.toCollection(TreeSet::new)),
                    manutencao.map(m -> (Set<String>) new TreeSet<>(m.bicicletasForaDeServico()))
                            .orElseGet(TreeSet::new));
            if (conteudo.equals(ultimoConteudo)) {
                return;
            }
            docas.disponibilizar(new PacoteAutorizacao(versao.incrementAndGet(), ChavesAssinaturaQr.ALGORITMO,
                    conteudo.chavePublica(), conteudo.cartoes(), conteudo.foraDeServico(), Instant.now()));
            ultimoConteudo = conteudo;
        } catch (RuntimeException e) {
            LOG.warn("Sincronização com as docas falhou; mantém-se o último pacote", e);
        }
    }

    private record ConteudoPacote(String chavePublica, Set<String> cartoes, Set<String> foraDeServico) {
    }
}
