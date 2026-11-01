package pt.lisboapedala.estacao.ocupacao.api;

import java.util.List;
import java.util.Optional;

/** Leitura da ocupação das estações. Usado por: disponibilidade, redistribuicao, relatorios. */
public interface Ocupacao {

    Optional<OcupacaoEstacao> daEstacao(String estacaoId);

    List<OcupacaoEstacao> todas();
}
