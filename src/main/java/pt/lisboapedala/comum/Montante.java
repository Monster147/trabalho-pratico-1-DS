package pt.lisboapedala.comum;

public record Montante(long centimos) {
    public Montante {
        if (centimos < 0) {
            throw new IllegalArgumentException("O montante não pode ser negativo");
        }
    }
}
