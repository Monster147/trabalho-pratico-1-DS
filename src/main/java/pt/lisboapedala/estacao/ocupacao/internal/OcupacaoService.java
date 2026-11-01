package pt.lisboapedala.estacao.ocupacao.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import pt.lisboapedala.estacao.docas.api.BicicletaEncaixada;
import pt.lisboapedala.estacao.docas.api.DesbloqueioEfetuado;
import pt.lisboapedala.estacao.ocupacao.api.Ocupacao;
import pt.lisboapedala.estacao.ocupacao.api.OcupacaoAtualizada;
import pt.lisboapedala.estacao.ocupacao.api.OcupacaoEstacao;

/**
 * Escrita da ocupação por doca, a partir dos eventos de docas.api. Em memória, como o resto do projeto.
 *
 * <ul>
 *   <li>{@link DesbloqueioEfetuado} → a doca fica livre; guarda-se viagemId → bicicletaId.</li>
 *   <li>{@link BicicletaEncaixada} → a doca fica ocupada. O evento não traz a bicicleta, por isso
 *       ela é obtida pelo viagemId (desbloqueio correspondente). Se o desbloqueio ainda não chegou
 *       (doca de origem offline), a doca conta como ocupada e a bicicleta é preenchida mais tarde.</li>
 * </ul>
 * Handlers idempotentes; eventos mais antigos do que o estado atual da doca são ignorados.
 */
@Service
public class OcupacaoService implements Ocupacao {

    private final Map<String, Estacao> estacoes = new ConcurrentHashMap<>();
    private final Map<String, EstadoDoca> docas = new ConcurrentHashMap<>();
    private final Map<String, String> bicicletaPorViagem = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher publicador;

    OcupacaoService(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoDesbloquear(DesbloqueioEfetuado evento) {
        Set<String> afetadas = new java.util.HashSet<>();
        synchronized (this) {
            bicicletaPorViagem.put(evento.viagemId(), evento.bicicletaId());

            EstadoDoca origem = docas.get(evento.docaId().valor());
            if (origem != null && !origem.atualizadoEm().isAfter(evento.ocorridoEm())) {
                docas.put(origem.docaId(), EstadoDoca.livre(origem.docaId(), origem.estacaoId(), evento.ocorridoEm()));
                afetadas.add(origem.estacaoId());
            }
            // Encaixe que chegou antes do desbloqueio: agora já se sabe que bicicleta é.
            docas.replaceAll((id, d) -> d.ocupada() && d.bicicletaId() == null
                    && evento.viagemId().equals(d.viagemId()) ? d.comBicicleta(evento.bicicletaId()) : d);
        }
        afetadas.forEach(this::notificar);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoEncaixar(BicicletaEncaixada evento) {
        String estacaoAfetada;
        synchronized (this) {
            EstadoDoca destino = docas.get(evento.docaId().valor());
            if (destino == null || destino.atualizadoEm().isAfter(evento.ocorridoEm())) {
                return; // doca não registada ou evento antigo
            }
            docas.put(destino.docaId(), new EstadoDoca(destino.docaId(), destino.estacaoId(), true,
                    evento.viagemId(), bicicletaPorViagem.get(evento.viagemId()), evento.ocorridoEm()));
            estacaoAfetada = destino.estacaoId();
        }
        notificar(estacaoAfetada);
    }

    @Override
    public Optional<OcupacaoEstacao> daEstacao(String estacaoId) {
        return Optional.ofNullable(estacoes.get(estacaoId)).map(this::fotografia);
    }

    @Override
    public List<OcupacaoEstacao> todas() {
        return estacoes.values().stream().map(this::fotografia).toList();
    }

    /** Operação interna (REST de administração): registo de estação e das suas docas. */
    public synchronized OcupacaoEstacao registarEstacao(Estacao estacao) {
        if (estacao.id() == null || estacao.id().isBlank() || estacao.docas().isEmpty()) {
            throw new IllegalArgumentException("Estação precisa de id e de pelo menos uma doca");
        }
        if (estacoes.containsKey(estacao.id())) {
            throw new IllegalArgumentException("Estação já existe: " + estacao.id());
        }
        for (String doca : estacao.docas()) {
            if (docas.containsKey(doca)) {
                throw new IllegalArgumentException("Doca já pertence a outra estação: " + doca);
            }
        }
        Instant agora = Instant.EPOCH; // qualquer evento real é mais recente do que o registo
        estacao.docas().forEach(d -> docas.put(d, EstadoDoca.livre(d, estacao.id(), agora)));
        estacoes.put(estacao.id(), estacao);
        return fotografia(estacao);
    }

    private OcupacaoEstacao fotografia(Estacao e) {
        List<EstadoDoca> estados = new ArrayList<>();
        e.docas().forEach(d -> Optional.ofNullable(docas.get(d)).ifPresent(estados::add));
        List<EstadoDoca> ocupadas = estados.stream().filter(EstadoDoca::ocupada).toList();
        Set<String> bicicletas = ocupadas.stream().map(EstadoDoca::bicicletaId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Instant atualizado = estados.stream().map(EstadoDoca::atualizadoEm).max(Instant::compareTo)
                .orElse(Instant.EPOCH);
        return new OcupacaoEstacao(e.id(), e.nome(), e.freguesia(), e.latitude(), e.longitude(),
                e.docas().size(), ocupadas.size(), bicicletas, atualizado);
    }

    private void notificar(String estacaoId) {
        daEstacao(estacaoId).ifPresent(o -> publicador.publishEvent(
                new OcupacaoAtualizada(estacaoId, o.docasOcupadas(), o.capacidade(), Instant.now())));
    }
}
