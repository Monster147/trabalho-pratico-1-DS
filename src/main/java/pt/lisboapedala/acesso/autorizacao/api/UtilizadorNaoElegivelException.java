package pt.lisboapedala.acesso.autorizacao.api;

import pt.lisboapedala.comum.UtilizadorId;

public class UtilizadorNaoElegivelException extends RuntimeException {

    private final String motivo;

    public UtilizadorNaoElegivelException(UtilizadorId utilizadorId, String motivo) {
        super("Utilizador " + utilizadorId.valor() + " não elegível: " + motivo);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
