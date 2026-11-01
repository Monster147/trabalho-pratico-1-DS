package pt.lisboapedala.comum;

import java.time.Instant;

/** Metadados mínimos partilhados por eventos do monólito. */
public interface EventoBase {
    Instant ocorridoEm();
}
