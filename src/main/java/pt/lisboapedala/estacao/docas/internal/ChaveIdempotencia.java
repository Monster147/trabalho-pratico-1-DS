package pt.lisboapedala.estacao.docas.internal;

/** Chave de idempotência (docaId, numeroSequencia) — ADR-003. */
record ChaveIdempotencia(String docaId, long numeroSequencia) {
}
