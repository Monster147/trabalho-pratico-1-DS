package pt.lisboapedala.comum;

public record DocaId(String valor) {
    public DocaId {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Doca inválida");
    }
}
