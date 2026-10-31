package pt.lisboapedala.viagem.historico.api;

import java.util.List;
import pt.lisboapedala.comum.UtilizadorId;

public interface Historico {
    List<HistoricoItem> consultar(UtilizadorId utilizadorId);
}
