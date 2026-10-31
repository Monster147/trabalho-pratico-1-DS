package pt.lisboapedala.cobranca.pagamentos.api;

import java.util.List;
import pt.lisboapedala.comum.UtilizadorId;

public interface Pagamentos {
    List<Divida> dividasDe(UtilizadorId utilizadorId);
}
