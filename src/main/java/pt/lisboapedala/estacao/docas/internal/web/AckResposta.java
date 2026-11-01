package pt.lisboapedala.estacao.docas.internal.web;

/** ACK devolvido à doca. Só é enviado depois de a chave de idempotência estar persistida. */
public record AckResposta(String docaId, long numeroSequencia, String resultado) {
}
