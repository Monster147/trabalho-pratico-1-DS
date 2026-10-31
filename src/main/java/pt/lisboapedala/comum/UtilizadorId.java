package pt.lisboapedala.comum;

public record UtilizadorId(String valor) {
    public UtilizadorId {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Utilizador inválido");
    }
}
