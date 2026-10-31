package pt.lisboapedala.cobranca.tarifario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pt.lisboapedala.cobranca.tarifario.api.ConfiguracaoTarifario;
import pt.lisboapedala.cobranca.tarifario.internal.TarifarioService;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.TipoUtilizador;

class TarifarioServiceTest {
    @Test
    void aplicaMinutosGratisEBlocos() {
        var municipio = new MunicipioId("lisboa");
        var tarifario = new TarifarioService();
        tarifario.configurar(new ConfiguracaoTarifario(municipio, 45, 15,
                new Montante(50), new Montante(100)));

        assertEquals(100, tarifario.calcular(Duration.ofMinutes(61),
                TipoUtilizador.ASSINANTE, municipio).centimos());
        assertEquals(200, tarifario.calcular(Duration.ofMinutes(61),
                TipoUtilizador.OCASIONAL, municipio).centimos());
    }
}
