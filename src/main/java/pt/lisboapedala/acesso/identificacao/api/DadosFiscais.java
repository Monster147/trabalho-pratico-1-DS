package pt.lisboapedala.acesso.identificacao.api;

import pt.lisboapedala.comum.UtilizadorId;

public record DadosFiscais(UtilizadorId utilizadorId, String nif) {
    public DadosFiscais {
        if (nif == null || !nif.matches("\\d{9}")) throw new IllegalArgumentException("NIF inválido");
    }
}
