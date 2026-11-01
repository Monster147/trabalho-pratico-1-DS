package pt.lisboapedala.acesso.identificacao.api;

import pt.lisboapedala.comum.UtilizadorId;

public record Elegibilidade(UtilizadorId utilizadorId, boolean elegivel, MotivoInelegibilidade motivo) {

    public static Elegibilidade elegivel(UtilizadorId id) {
        return new Elegibilidade(id, true, MotivoInelegibilidade.NENHUM);
    }

    public static Elegibilidade inelegivel(UtilizadorId id, MotivoInelegibilidade motivo) {
        return new Elegibilidade(id, false, motivo);
    }
}
