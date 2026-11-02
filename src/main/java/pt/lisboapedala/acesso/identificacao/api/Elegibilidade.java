package pt.lisboapedala.acesso.identificacao.api;

import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/** @param tipoUtilizador tipo da conta; nulo quando a conta não existe */
public record Elegibilidade(UtilizadorId utilizadorId, boolean elegivel, MotivoInelegibilidade motivo,
                            TipoUtilizador tipoUtilizador) {

    public static Elegibilidade elegivel(UtilizadorId id, TipoUtilizador tipo) {
        return new Elegibilidade(id, true, MotivoInelegibilidade.NENHUM, tipo);
    }

    public static Elegibilidade inelegivel(UtilizadorId id, MotivoInelegibilidade motivo, TipoUtilizador tipo) {
        return new Elegibilidade(id, false, motivo, tipo);
    }
}
