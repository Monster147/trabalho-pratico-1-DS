package pt.lisboapedala.estacao.ocupacao.api;

import java.time.Instant;
import java.util.Set;

/**
 * Fotografia da ocupação de uma estação.
 *
 * @param capacidade           número de docas da estação
 * @param docasOcupadas        docas com bicicleta encaixada
 * @param bicicletasConhecidas identificadores das bicicletas encaixadas que já se conhecem
 *                             (pode ter menos elementos do que docasOcupadas, ver OcupacaoService)
 */
public record OcupacaoEstacao(String estacaoId, String nome, String freguesia, double latitude, double longitude,
                              int capacidade, int docasOcupadas, Set<String> bicicletasConhecidas,
                              Instant atualizadoEm) {

    public OcupacaoEstacao {
        bicicletasConhecidas = Set.copyOf(bicicletasConhecidas);
    }

    public int docasLivres() {
        return Math.max(0, capacidade - docasOcupadas);
    }
}
