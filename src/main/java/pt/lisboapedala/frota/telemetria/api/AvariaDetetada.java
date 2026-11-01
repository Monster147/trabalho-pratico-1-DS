package pt.lisboapedala.frota.telemetria.api;

import java.time.Instant;
import pt.lisboapedala.comum.EventoBase;

public record AvariaDetetada(String bicicletaId, String descricao, Instant ocorridoEm) implements EventoBase {
    public AvariaDetetada {
        if (bicicletaId == null || bicicletaId.isBlank()) {
            throw new IllegalArgumentException("Bicicleta inválida");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da avaria obrigatória");
        }
        if (ocorridoEm == null) {
            throw new NullPointerException("ocorridoEm");
        }
    }
}
