package pt.lisboapedala.estacao.ocupacao.internal;

import java.time.Instant;

/**
 * Estado de uma doca física.
 *
 * @param viagemId    viagem que terminou nesta doca (vem no BicicletaEncaixada)
 * @param bicicletaId bicicleta encaixada, se já for conhecida
 */
record EstadoDoca(String docaId, String estacaoId, boolean ocupada, String viagemId, String bicicletaId,
                  Instant atualizadoEm) {

    static EstadoDoca livre(String docaId, String estacaoId, Instant em) {
        return new EstadoDoca(docaId, estacaoId, false, null, null, em);
    }

    EstadoDoca comBicicleta(String bicicleta) {
        return new EstadoDoca(docaId, estacaoId, ocupada, viagemId, bicicleta, atualizadoEm);
    }
}
