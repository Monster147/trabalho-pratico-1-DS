package pt.lisboapedala.estacao.disponibilidade.internal.web;

import java.time.Instant;

public record EstacaoMapaResposta(String estacaoId, String nome, double latitude, double longitude,
                                  int bicicletasDisponiveis, int bicicletasBateriaBaixa, int docasLivres,
                                  Instant atualizadoEm) {
}
