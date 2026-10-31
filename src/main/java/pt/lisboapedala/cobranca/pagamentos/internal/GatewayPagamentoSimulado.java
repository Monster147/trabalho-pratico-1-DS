package pt.lisboapedala.cobranca.pagamentos.internal;

import org.springframework.stereotype.Component;
import pt.lisboapedala.cobranca.faturacao.api.Fatura;

@Component
class GatewayPagamentoSimulado implements GatewayPagamento {
    @Override
    public boolean cobrar(Fatura fatura) {
        return true;
    }
}
