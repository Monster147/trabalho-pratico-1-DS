package pt.lisboapedala.cobranca.faturacao.api;

import java.util.List;
import pt.lisboapedala.comum.UtilizadorId;

public interface Faturacao {
    List<Fatura> doUtilizador(UtilizadorId utilizadorId);
}
