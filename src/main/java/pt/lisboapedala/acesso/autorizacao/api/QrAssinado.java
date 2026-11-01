package pt.lisboapedala.acesso.autorizacao.api;

import java.time.Instant;

/**
 * Conteúdo do QR: {@code LP1.<payload Base64URL>.<assinatura Base64URL>}.
 * payload = {@code utilizadorId|emitidoEm(epoch s)|expiraEm(epoch s)|nonce}.
 */
public record QrAssinado(String conteudo, Instant expiraEm) {
}
