package pt.lisboapedala.estacao.docas.internal;

import java.time.Instant;

/**
 * Dados enviados pela doca física. Campos usados por tipo:
 * DESBLOQUEIO_EFETUADO → utilizadorId, bicicletaId, municipioId; BICICLETA_ENCAIXADA → viagemId.
 */
public record EventoDocaRecebido(String docaId, long numeroSequencia, TipoEventoDoca tipo,
                                 String utilizadorId, String bicicletaId, String municipioId,
                                 String viagemId, Instant ocorridoEm) {

    ChaveIdempotencia chave() {
        return new ChaveIdempotencia(docaId, numeroSequencia);
    }

    /** Lança IllegalArgumentException se faltar algum campo obrigatório para o tipo. */
    public void validar() {
        exigir(docaId, "docaId");
        if (tipo == null) {
            throw new IllegalArgumentException("tipo é obrigatório");
        }
        if (numeroSequencia <= 0) {
            throw new IllegalArgumentException("numeroSequencia tem de ser positivo");
        }
        if (ocorridoEm == null) {
            throw new IllegalArgumentException("ocorridoEm é obrigatório");
        }
        if (tipo == TipoEventoDoca.DESBLOQUEIO_EFETUADO) {
            exigir(utilizadorId, "utilizadorId");
            exigir(bicicletaId, "bicicletaId");
            exigir(municipioId, "municipioId");
        } else {
            exigir(viagemId, "viagemId");
        }
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
    }
}
