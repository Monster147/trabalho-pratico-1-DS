package pt.lisboapedala.acesso.identificacao.api;

import pt.lisboapedala.comum.UtilizadorId;

public interface DadosFiscaisProvider {
    DadosFiscais obterDadosFiscais(UtilizadorId utilizadorId);
}
