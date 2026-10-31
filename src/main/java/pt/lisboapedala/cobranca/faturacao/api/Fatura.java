package pt.lisboapedala.cobranca.faturacao.api;

import java.time.Instant;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.UtilizadorId;

public record Fatura(String numero, String viagemId, UtilizadorId utilizadorId,
                     String nif, Montante montante, Instant emitidaEm) {
}
