package pt.lisboapedala.viagem.viagens.internal;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.estacao.docas.api.BicicletaEncaixada;
import pt.lisboapedala.estacao.docas.api.DesbloqueioEfetuado;
import pt.lisboapedala.viagem.viagens.api.Viagem;
import pt.lisboapedala.viagem.viagens.api.ViagemFechada;
import pt.lisboapedala.viagem.viagens.api.Viagens;

@Service
public class ViagensService implements Viagens {
    private final ConcurrentMap<String, Viagem> viagens = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher events;

    public ViagensService(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional
    public void abrir(DesbloqueioEfetuado evento, TipoUtilizador tipo) {
        viagens.putIfAbsent(evento.viagemId(), new Viagem(evento.viagemId(),
                evento.utilizadorId(), evento.bicicletaId(), evento.municipioId(),
                tipo, evento.ocorridoEm(), null));
    }

    @Transactional
    public void fechar(BicicletaEncaixada evento) {
        Viagem atual = viagens.get(evento.viagemId());
        if (atual == null) throw new IllegalStateException("Viagem inexistente: " + evento.viagemId());
        if (atual.terminadaEm() != null) return;
        Viagem fechada = new Viagem(atual.id(), atual.utilizadorId(), atual.bicicletaId(),
                atual.municipioId(), atual.tipoUtilizador(), atual.iniciadaEm(),
                evento.ocorridoEm());
        viagens.put(fechada.id(), fechada);
        events.publishEvent(new ViagemFechada(fechada));
    }

    public void abrir(DesbloqueioEfetuado evento) {
        abrir(evento, TipoUtilizador.OCASIONAL);
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
