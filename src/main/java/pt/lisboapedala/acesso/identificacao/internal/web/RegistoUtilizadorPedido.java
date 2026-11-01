package pt.lisboapedala.acesso.identificacao.internal.web;

import pt.lisboapedala.comum.TipoUtilizador;

public record RegistoUtilizadorPedido(String nome, String email, String nif, TipoUtilizador tipo,
                                      String numeroCartaoNavegante) {
}
