package pt.lisboapedala.estacao.ocupacao.api;

import java.time.Instant;

/** Publicado sempre que a ocupação de uma estação muda. */
public record OcupacaoAtualizada(String estacaoId, int docasOcupadas, int capacidade, Instant ocorridoEm) {
}
