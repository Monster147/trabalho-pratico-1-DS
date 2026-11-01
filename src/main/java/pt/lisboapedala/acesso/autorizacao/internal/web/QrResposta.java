package pt.lisboapedala.acesso.autorizacao.internal.web;

import java.time.Instant;

public record QrResposta(String conteudo, Instant expiraEm) {
}
