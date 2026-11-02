package pt.lisboapedala.cobranca.tarifario.internal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import pt.lisboapedala.cobranca.tarifario.api.ConfiguracaoTarifario;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.MunicipioId;

/** Carrega no arranque os tarifários de cada município definidos na configuração. */
@Configuration
@EnableConfigurationProperties(PropriedadesTarifario.class)
class TarifariosIniciais {

    TarifariosIniciais(TarifarioService tarifario, PropriedadesTarifario propriedades) {
        propriedades.municipios().forEach((municipio, r) -> tarifario.configurar(new ConfiguracaoTarifario(
                new MunicipioId(municipio), r.minutosGratis(), r.blocoMinutos(),
                new Montante(r.precoPorBlocoCentimos()), new Montante(r.precoBaseOcasionalCentimos()))));
    }
}
