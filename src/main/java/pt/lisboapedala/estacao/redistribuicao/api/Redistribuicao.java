package pt.lisboapedala.estacao.redistribuicao.api;

/** Sugestão de movimentos de reequilíbrio para a equipa de redistribuição. */
public interface Redistribuicao {

    /** @param capacidadeVeiculo máximo de bicicletas transportadas por movimento */
    PlanoRedistribuicao sugerirPlano(int capacidadeVeiculo);
}
