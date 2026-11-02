package pt.lisboapedala.viagem.viagens.internal;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.estacao.docas.api.BicicletaEncaixada;
import pt.lisboapedala.estacao.docas.api.DesbloqueioEfetuado;
import pt.lisboapedala.viagem.viagens.api.Viagem;
import pt.lisboapedala.viagem.viagens.api.ViagemFechada;
import pt.lisboapedala.viagem.viagens.api.Viagens;

/**
 * Ciclo de vida da viagem, a partir dos eventos já deduplicados por docas (ADR-003).
 *
 * <ul>
 *   <li>Os listeners correm depois do commit da receção em docas, cada um na sua própria
 *       unidade de trabalho (REQUIRES_NEW): fechar a viagem e publicar ViagemFechada acontecem
 *       juntos, e faturacao só reage depois desse commit.</li>
 *   <li>Abrir e fechar são idempotentes: um reenvio não duplica viagens nem faturas (US-3).</li>
 *   <li>Se o encaixe chegar antes do desbloqueio (doca de origem estava sem rede), fica pendente
 *       e a viagem fecha logo que o desbloqueio chegar.</li>
 * </ul>
 */
@Service
public class ViagensService implements Viagens {
    private final ConcurrentMap<String, Viagem> viagens = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BicicletaEncaixada> encaixesPendentes = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher events;

    public ViagensService(ApplicationEventPublisher events) {
        this.events = events;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void quandoDesbloqueio(DesbloqueioEfetuado evento) {
        synchronized (this) {
            registarAbertura(evento, evento.tipoUtilizador());
            BicicletaEncaixada pendente = encaixesPendentes.remove(evento.viagemId());
            if (pendente != null) {
                registarFecho(pendente);
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void quandoEncaixe(BicicletaEncaixada evento) {
        synchronized (this) {
            if (!viagens.containsKey(evento.viagemId())) {
                encaixesPendentes.putIfAbsent(evento.viagemId(), evento);
                return;
            }
            registarFecho(evento);
        }
    }

    @Transactional
    public void abrir(DesbloqueioEfetuado evento, TipoUtilizador tipo) {
        registarAbertura(evento, tipo);
    }

    @Transactional
    public void fechar(BicicletaEncaixada evento) {
        if (!viagens.containsKey(evento.viagemId())) {
            throw new IllegalStateException("Viagem inexistente: " + evento.viagemId());
        }
        registarFecho(evento);
    }

    public void abrir(DesbloqueioEfetuado evento) {
        abrir(evento, evento.tipoUtilizador() != null ? evento.tipoUtilizador() : TipoUtilizador.OCASIONAL);
    }

    private void registarAbertura(DesbloqueioEfetuado evento, TipoUtilizador tipo) {
        viagens.putIfAbsent(evento.viagemId(), new Viagem(evento.viagemId(),
                evento.utilizadorId(), evento.bicicletaId(), evento.municipioId(),
                tipo, evento.ocorridoEm(), null));
    }

    private void registarFecho(BicicletaEncaixada evento) {
        Viagem atual = viagens.get(evento.viagemId());
        if (atual.terminadaEm() != null) return;
        Viagem fechada = new Viagem(atual.id(), atual.utilizadorId(), atual.bicicletaId(),
                atual.municipioId(), atual.tipoUtilizador(), atual.iniciadaEm(),
                evento.ocorridoEm());
        viagens.put(fechada.id(), fechada);
        events.publishEvent(new ViagemFechada(fechada));
    }

    @Override
    public Viagem porId(String id) {
        Viagem viagem = viagens.get(id);
        if (viagem == null) throw new IllegalArgumentException("Viagem inexistente: " + id);
        return viagem;
    }

    @Override
    public List<Viagem> doUtilizador(UtilizadorId utilizadorId) {
        return viagens.values().stream().filter(v -> v.utilizadorId().equals(utilizadorId)).toList();
    }

    @Override
    public List<Viagem> todas() {
        return List.copyOf(viagens.values());
    }
}
