package pt.lisboapedala.estacao.docas.internal.web;

import java.time.Instant;

import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.estacao.docas.internal.TipoEventoDoca;

/**
 * Corpo enviado pela doca física (K3).
 * DESBLOQUEIO_EFETUADO: utilizadorId, bicicletaId, municipioId, tipoUtilizador. BICICLETA_ENCAIXADA: viagemId.
 */
public record EventoDocaPedido(TipoEventoDoca tipo, long numeroSequencia, String utilizadorId,
                               String bicicletaId, String municipioId, TipoUtilizador tipoUtilizador,
                               String viagemId, Instant ocorridoEm) {
}
