package pt.lisboapedala.viagem.viagens.api;

import java.util.List;
import pt.lisboapedala.comum.UtilizadorId;

public interface Viagens {
    Viagem porId(String id);
    List<Viagem> doUtilizador(UtilizadorId utilizadorId);
    List<Viagem> todas();
}
