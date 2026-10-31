package pt.lisboapedala.cobranca.faturacao.internal;

import org.springframework.stereotype.Component;
import pt.lisboapedala.cobranca.faturacao.api.Fatura;

@Component
class AutoridadeTributariaSimulada implements AutoridadeTributaria {
    @Override
    public void comunicar(Fatura fatura) {
    }
}
