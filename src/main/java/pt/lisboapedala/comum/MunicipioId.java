package pt.lisboapedala.comum;

public record MunicipioId(String valor) {
    public MunicipioId {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Município inválido");
    }
}
