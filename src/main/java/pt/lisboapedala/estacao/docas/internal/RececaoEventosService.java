package pt.lisboapedala.estacao.docas.internal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.lisboapedala.comum.DocaId;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.estacao.docas.api.BicicletaEncaixada;
import pt.lisboapedala.estacao.docas.api.DesbloqueioEfetuado;

/**
 * Receção idempotente de eventos das docas (ADR-003):
 * <ol>
 *   <li>eventos da mesma doca são processados em série;</li>
 *   <li>se a chave (docaId, numeroSequencia) já existe → DUPLICADO, sem republicar;</li>
 *   <li>a chave e o evento são persistidos (com fsync) ANTES de se responder;</li>
 *   <li>publicam-se, por ordem de sequência, todos os eventos consecutivos disponíveis.</li>
 * </ol>
 * Os eventos públicos são os de docas.api já usados por viagens ({@link DesbloqueioEfetuado},
 * {@link BicicletaEncaixada}). Depois de um reinício, os eventos persistidos mas ainda não
 * publicados são retomados (entrega pelo menos uma vez: os consumidores devem ser idempotentes).
 */
@Service
public class RececaoEventosService {

    private static final Logger LOG = LoggerFactory.getLogger(RececaoEventosService.class);

    private final DiarioEventosDocas diario;
    private final ApplicationEventPublisher publicador;
    private final Map<String, Object> bloqueiosPorDoca = new ConcurrentHashMap<>();

    RececaoEventosService(DiarioEventosDocas diario, ApplicationEventPublisher publicador) {
        this.diario = diario;
        this.publicador = publicador;
    }

    @Transactional
    public ResultadoRececao receber(EventoDocaRecebido evento) {
        evento.validar();
        synchronized (bloqueio(evento.docaId())) {
            if (diario.contem(evento.chave())) {
                return ResultadoRececao.DUPLICADO;
            }
            diario.registarRececao(evento);

            Optional<Long> ultima = diario.ultimaPublicada(evento.docaId());
            if (ultima.isEmpty()) {
                // Primeira vez que esta doca comunica: o primeiro evento define a linha de base.
                diario.registarPublicacao(evento.docaId(), evento.numeroSequencia() - 1);
            } else if (evento.numeroSequencia() <= ultima.get()) {
                LOG.warn("Evento atrasado da doca {} (seq {}) publicado fora de ordem",
                        evento.docaId(), evento.numeroSequencia());
                publicar(evento);
                return ResultadoRececao.ACEITE;
            }
            long publicadaAte = publicarConsecutivos(evento.docaId());
            return publicadaAte >= evento.numeroSequencia()
                    ? ResultadoRececao.ACEITE
                    : ResultadoRececao.ACEITE_AGUARDA_ORDEM;
        }
    }

    public boolean jaRecebido(String docaId, long numeroSequencia) {
        return diario.contem(new ChaveIdempotencia(docaId, numeroSequencia));
    }

    /** Recuperação após reinício: publica o que ficou persistido mas não publicado. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void retomarPendentes() {
        for (String docaId : diario.docasConhecidas()) {
            synchronized (bloqueio(docaId)) {
                publicarConsecutivos(docaId);
            }
        }
    }

    private long publicarConsecutivos(String docaId) {
        long ultima = diario.ultimaPublicada(docaId).orElseThrow();
        Optional<EventoDocaRecebido> seguinte;
        while ((seguinte = diario.obter(new ChaveIdempotencia(docaId, ultima + 1))).isPresent()) {
            publicar(seguinte.get());
            ultima++;
            diario.registarPublicacao(docaId, ultima);
        }
        return ultima;
    }

    private void publicar(EventoDocaRecebido e) {
        var docaId = new DocaId(e.docaId());
        Object publico = switch (e.tipo()) {
            case DESBLOQUEIO_EFETUADO -> new DesbloqueioEfetuado(docaId, e.numeroSequencia(),
                    new UtilizadorId(e.utilizadorId()), e.bicicletaId(), new MunicipioId(e.municipioId()),
                    e.tipoUtilizador(), e.ocorridoEm());
            case BICICLETA_ENCAIXADA -> new BicicletaEncaixada(docaId, e.numeroSequencia(), e.viagemId(),
                    e.ocorridoEm());
        };
        publicador.publishEvent(publico);
    }

    private Object bloqueio(String docaId) {
        return bloqueiosPorDoca.computeIfAbsent(docaId, k -> new Object());
    }
}
