package pt.lisboapedala.cobranca.pagamentos.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.lisboapedala.cobranca.faturacao.api.FaturaEmitida;
import pt.lisboapedala.cobranca.pagamentos.api.Divida;
import pt.lisboapedala.cobranca.pagamentos.api.Pagamentos;
import pt.lisboapedala.comum.UtilizadorId;

@Service
public class PagamentosService implements Pagamentos {
    private final GatewayPagamento gateway;
    private final Map<String, Divida> dividas = new ConcurrentHashMap<>();

    public PagamentosService(GatewayPagamento gateway) {
        this.gateway = gateway;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void quandoFaturaEmitida(FaturaEmitida evento) {
        var fatura = evento.fatura();
        if (gateway.cobrar(fatura)) {
            dividas.remove(fatura.numero());
            return;
        }
        registarFalha(fatura);
    }

    public void registarFalha(pt.lisboapedala.cobranca.faturacao.api.Fatura fatura) {
        Divida anterior = dividas.get(fatura.numero());
        int tentativas = anterior == null ? 1 : anterior.tentativas() + 1;
        dividas.put(fatura.numero(), new Divida(fatura.numero(), fatura.utilizadorId(),
                fatura.montante(), tentativas, Instant.now().plus(tentativas, ChronoUnit.MINUTES)));
    }

    @Override
    public List<Divida> dividasDe(UtilizadorId utilizadorId) {
        return dividas.values().stream().filter(d -> d.utilizadorId().equals(utilizadorId)).toList();
    }
}
