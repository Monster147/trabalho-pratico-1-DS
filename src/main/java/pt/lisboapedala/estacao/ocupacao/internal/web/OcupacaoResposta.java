package pt.lisboapedala.estacao.ocupacao.internal.web;

import java.time.Instant;

public record OcupacaoResposta(String estacaoId, String nome, String freguesia, double latitude, double longitude,
                               int capacidade, int docasOcupadas, int docasLivres, Instant atualizadoEm) {
}
