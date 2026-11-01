package pt.lisboapedala.frota.telemetria.api;

import java.time.Instant;
import java.util.Objects;
import pt.lisboapedala.comum.EventoBase;

public record EstadoBicicletaReportado(
        String bicicletaId, int bateriaPercentagem, double latitude, double longitude, Instant ocorridoEm)
        implements EventoBase {
    public EstadoBicicletaReportado {
        if (bicicletaId == null || bicicletaId.isBlank()) {
            throw new IllegalArgumentException("Bicicleta inválida");
        }
        if (bateriaPercentagem < 0 || bateriaPercentagem > 100) {
            throw new IllegalArgumentException("A bateria deve estar entre 0 e 100");
        }
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
    }
}
