package pt.lisboapedala.frota.manutencao.api;

import java.util.Set;

/**
 * PROVISÓRIO — contrato mínimo assumido por autorizacao e disponibilidade.
 * Substituir pela API real do componente manutencao. Sem implementação aqui de propósito:
 * enquanto não existir um bean, os consumidores assumem que nenhuma bicicleta está fora de serviço.
 */
public interface ManutencaoApi {

    /** Identificadores das bicicletas atualmente fora de serviço. */
    Set<String> bicicletasForaDeServico();
}
