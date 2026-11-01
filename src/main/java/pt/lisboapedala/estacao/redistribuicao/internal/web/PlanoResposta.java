package pt.lisboapedala.estacao.redistribuicao.internal.web;

import java.time.Instant;
import java.util.List;

public record PlanoResposta(List<MovimentoResposta> movimentos, Instant geradoEm) {
}
