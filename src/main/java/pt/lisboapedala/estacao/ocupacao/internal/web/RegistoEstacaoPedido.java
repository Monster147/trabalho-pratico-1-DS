package pt.lisboapedala.estacao.ocupacao.internal.web;

import java.util.List;

public record RegistoEstacaoPedido(String id, String nome, String freguesia, double latitude, double longitude,
                                   List<String> docas) {
}
