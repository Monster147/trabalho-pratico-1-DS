package pt.lisboapedala.cobranca.tarifario.internal;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import pt.lisboapedala.cobranca.tarifario.api.ConfiguracaoTarifario;
import pt.lisboapedala.cobranca.tarifario.api.Tarifario;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.TipoUtilizador;

@Service
public class TarifarioService implements Tarifario {
    private final Map<MunicipioId, ConfiguracaoTarifario> configuracoes = new ConcurrentHashMap<>();

    public void configurar(ConfiguracaoTarifario configuracao) {
        configuracoes.put(configuracao.municipioId(), configuracao);
    }

    @Override
    public Montante calcular(Duration duracao, TipoUtilizador tipo, MunicipioId municipioId) {
        if (duracao.isNegative()) throw new IllegalArgumentException("Duração inválida");
        ConfiguracaoTarifario config = configuracoes.get(municipioId);
        if (config == null) throw new IllegalArgumentException("Tarifário inexistente: " + municipioId.valor());
        long minutos = (duracao.toSeconds() + 59) / 60;
        long cobrados = Math.max(0, minutos - config.minutosGratis());
        long blocos = (cobrados + config.blocoMinutos() - 1) / config.blocoMinutos();
        long total = Math.multiplyExact(blocos, config.precoPorBloco().centimos());
        if (tipo == TipoUtilizador.OCASIONAL) total = Math.addExact(total, config.precoBaseOcasional().centimos());
        return new Montante(total);
    }
}
