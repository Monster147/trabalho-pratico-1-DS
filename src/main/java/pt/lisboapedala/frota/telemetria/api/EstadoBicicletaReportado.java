package pt.lisboapedala.frota.telemetria.api;

import java.time.Instant;

/**
 * PROVISÓRIO — evento mínimo assumido (telemetria a cada 30 s).
 * Substituir pelo evento real do componente telemetria.
 */
public record EstadoBicicletaReportado(
        String bicicletaId, int bateriaPercentagem, double latitude, double longitude, Instant ocorridoEm) {
}
