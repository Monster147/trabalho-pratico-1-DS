package pt.lisboapedala.cobranca.pagamentos.api;

import java.time.Instant;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.UtilizadorId;

public record Divida(String faturaNumero, UtilizadorId utilizadorId,
                     Montante montante, int tentativas, Instant proximaTentativa) {
}
