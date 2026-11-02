package pt.lisboapedala.cobranca.faturacao.internal;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.lisboapedala.acesso.identificacao.api.DadosFiscais;
import pt.lisboapedala.acesso.identificacao.api.DadosFiscaisProvider;
import pt.lisboapedala.cobranca.faturacao.api.Fatura;
import pt.lisboapedala.cobranca.faturacao.api.FaturaEmitida;
import pt.lisboapedala.cobranca.faturacao.api.Faturacao;
import pt.lisboapedala.cobranca.tarifario.api.Tarifario;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.viagem.viagens.api.ViagemFechada;

@Service
public class FaturacaoService implements Faturacao {
    private final AtomicLong sequencia = new AtomicLong();
    private final List<Fatura> faturas = new CopyOnWriteArrayList<>();
    private final Tarifario tarifario;
    private final DadosFiscaisProvider dadosFiscais;
    private final AutoridadeTributaria autoridadeTributaria;
    private final ApplicationEventPublisher events;

    public FaturacaoService(Tarifario tarifario, DadosFiscaisProvider dadosFiscais,
                            AutoridadeTributaria autoridadeTributaria,
                            ApplicationEventPublisher events) {
        this.tarifario = tarifario;
        this.dadosFiscais = dadosFiscais;
        this.autoridadeTributaria = autoridadeTributaria;
        this.events = events;
    }

    /**
     * Emitir a fatura é uma unidade de trabalho própria (REQUIRES_NEW): sem ela, FaturaEmitida era
     * publicado fora de qualquer transação e o listener AFTER_COMMIT de pagamentos nunca corria.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void quandoViagemFechada(ViagemFechada evento) {
        var viagem = evento.viagem();
        DadosFiscais fiscais = dadosFiscais.obterDadosFiscais(viagem.utilizadorId());
        var montante = tarifario.calcular(viagem.duracao(), viagem.tipoUtilizador(), viagem.municipioId());
        Fatura fatura = new Fatura("FT-" + sequencia.incrementAndGet(), viagem.id(),
                viagem.utilizadorId(), fiscais.nif(), montante, Instant.now());
        faturas.add(fatura);
        autoridadeTributaria.comunicar(fatura);
        events.publishEvent(new FaturaEmitida(fatura));
    }

    @Override
    public List<Fatura> doUtilizador(UtilizadorId utilizadorId) {
        return faturas.stream().filter(f -> f.utilizadorId().equals(utilizadorId)).toList();
    }
}
