package pt.lisboapedala.cobranca.faturacao.internal;

import pt.lisboapedala.cobranca.faturacao.api.Fatura;

interface AutoridadeTributaria {
    void comunicar(Fatura fatura);
}
