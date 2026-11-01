package pt.lisboapedala.municipio.relatorios.internal;

import java.time.Duration;
import java.time.YearMonth;
import java.util.Objects;
import org.springframework.stereotype.Service;
import pt.lisboapedala.estacao.ocupacao.api.Ocupacao;
import pt.lisboapedala.municipio.relatorios.api.RelatorioMensal;
import pt.lisboapedala.municipio.relatorios.api.RelatoriosApi;
import pt.lisboapedala.viagem.viagens.api.Viagem;
import pt.lisboapedala.viagem.viagens.api.Viagens;

@Service
public class RelatoriosService implements RelatoriosApi {
    private static final double CO2_KG_POR_KM_AUTO = 0.192;
    private static final double VELOCIDADE_MEDIA_KMH = 12.0;

    private final Viagens viagens;
    private final Ocupacao ocupacao;

    public RelatoriosService(Viagens viagens, Ocupacao ocupacao) {
        this.viagens = viagens;
        this.ocupacao = ocupacao;
    }

    @Override
    public RelatorioMensal gerar(YearMonth mes, String freguesia) {
        Objects.requireNonNull(mes, "mes");
        if (freguesia == null || freguesia.isBlank()) {
            throw new IllegalArgumentException("Freguesia obrigatória");
        }
        var viagensDoMes = viagens.todas().stream()
                .filter(v -> concluidaNoMes(v, mes))
                .toList();
        long minutos = viagensDoMes.stream()
                .mapToLong(v -> Duration.between(v.iniciadaEm(), v.terminadaEm()).toMinutes())
                .sum();
        long bicicletas = ocupacao.todas().stream()
                .filter(o -> freguesia.equalsIgnoreCase(o.freguesia()))
                .mapToLong(o -> o.bicicletasConhecidas().size())
                .sum();
        double horas = minutos / 60.0;
        double co2 = horas * VELOCIDADE_MEDIA_KMH * CO2_KG_POR_KM_AUTO;
        return new RelatorioMensal(mes, freguesia, viagensDoMes.size(), minutos, bicicletas, co2);
    }

    private static boolean concluidaNoMes(Viagem viagem, YearMonth mes) {
        return viagem.terminadaEm() != null && YearMonth.from(viagem.terminadaEm()).equals(mes);
    }
}
