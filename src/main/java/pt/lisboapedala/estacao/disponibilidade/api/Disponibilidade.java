package pt.lisboapedala.estacao.disponibilidade.api;

import java.util.List;
import java.util.Optional;

/**
 * Vista de leitura do mapa em tempo real (C2). Responde só a partir da cache em memória:
 * nenhuma consulta compete com a escrita de viagens/ocupação.
 */
public interface Disponibilidade {

    List<DisponibilidadeEstacao> mapa();

    Optional<DisponibilidadeEstacao> daEstacao(String estacaoId);
}
