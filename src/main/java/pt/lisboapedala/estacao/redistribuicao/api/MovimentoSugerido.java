package pt.lisboapedala.estacao.redistribuicao.api;

/** Levar {@code quantidade} bicicletas da estação {@code origem} (cheia) para {@code destino} (vazia). */
public record MovimentoSugerido(String origem, String destino, int quantidade, double distanciaKm) {
}
