package pt.lisboapedala.estacao.disponibilidade.api;

import java.time.Instant;

/**
 * @param bicicletasDisponiveis  encaixadas e não fora de serviço
 * @param bicicletasBateriaBaixa das disponíveis, quantas reportaram bateria abaixo do limiar
 * @param docasLivres            docas sem bicicleta (avariadas também ocupam doca)
 */
public record DisponibilidadeEstacao(String estacaoId, String nome, double latitude, double longitude,
                                     int bicicletasDisponiveis, int bicicletasBateriaBaixa, int docasLivres,
                                     Instant atualizadoEm) {
}
