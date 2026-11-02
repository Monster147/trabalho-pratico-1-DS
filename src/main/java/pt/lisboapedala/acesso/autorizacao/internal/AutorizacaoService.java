package pt.lisboapedala.acesso.autorizacao.internal;

import org.springframework.stereotype.Service;

import pt.lisboapedala.acesso.autorizacao.api.Autorizacao;
import pt.lisboapedala.acesso.autorizacao.api.QrAssinado;
import pt.lisboapedala.acesso.autorizacao.api.UtilizadorNaoElegivelException;
import pt.lisboapedala.acesso.identificacao.api.Elegibilidade;
import pt.lisboapedala.acesso.identificacao.api.ElegibilidadeProvider;
import pt.lisboapedala.comum.UtilizadorId;

/** K1 + K5: valida a elegibilidade em identificacao e emite o QR assinado. */
@Service
class AutorizacaoService implements Autorizacao {

    private final ElegibilidadeProvider elegibilidade;
    private final EmissorQr emissor;

    AutorizacaoService(ElegibilidadeProvider elegibilidade, EmissorQr emissor) {
        this.elegibilidade = elegibilidade;
        this.emissor = emissor;
    }

    @Override
    public QrAssinado emitirQr(UtilizadorId utilizadorId) {
        Elegibilidade e = elegibilidade.verificar(utilizadorId);
        if (!e.elegivel()) {
            throw new UtilizadorNaoElegivelException(utilizadorId, e.motivo().name());
        }
        return emissor.emitir(utilizadorId, e.tipoUtilizador());
    }
}
