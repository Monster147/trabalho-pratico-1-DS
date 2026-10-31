package pt.lisboapedala.cobranca.pagamentos.internal;

import pt.lisboapedala.cobranca.faturacao.api.Fatura;

interface GatewayPagamento {
    boolean cobrar(Fatura fatura);
}
