package pt.lisboapedala.comum;

import java.util.Objects;

public record Montante(long centimos) {
    public Montante {
        if (centimos < 0) {
            throw new IllegalArgumentException("O montante não pode ser negativo");
        }
    }

    public Montante somar(Montante outro) {
        Objects.requireNonNull(outro, "outro");
        return new Montante(Math.addExact(centimos, outro.centimos));
    }
}
