package pt.lisboapedala.cobranca.tarifario.api;

import java.time.Duration;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.TipoUtilizador;

public interface Tarifario {
    Montante calcular(Duration duracao, TipoUtilizador tipo, MunicipioId municipioId);
}
