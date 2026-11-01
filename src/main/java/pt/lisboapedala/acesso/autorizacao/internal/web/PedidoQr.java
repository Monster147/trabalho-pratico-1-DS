package pt.lisboapedala.acesso.autorizacao.internal.web;

/** Simplificação: em produção o utilizador vem do token autenticado, não do corpo. */
public record PedidoQr(String utilizadorId) {
}
