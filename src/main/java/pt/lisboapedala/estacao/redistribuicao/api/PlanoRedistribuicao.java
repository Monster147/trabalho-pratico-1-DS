package pt.lisboapedala.estacao.redistribuicao.api;

import java.time.Instant;
import java.util.List;

public record PlanoRedistribuicao(List<MovimentoSugerido> movimentos, Instant geradoEm) {

    public PlanoRedistribuicao {
        movimentos = List.copyOf(movimentos);
    }
}
