package pt.lisboapedala.estacao.docas.api;

import java.time.Instant;
import pt.lisboapedala.comum.DocaId;

public record BicicletaEncaixada(
        DocaId docaId, long numeroSequencia, String viagemId, Instant ocorridoEm) {
}
