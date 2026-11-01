package pt.lisboapedala.frota.manutencao.internal;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import pt.lisboapedala.frota.manutencao.api.ManutencaoApi;
import pt.lisboapedala.frota.manutencao.api.OrdemManutencao;
import pt.lisboapedala.frota.telemetria.api.AvariaDetetada;
import pt.lisboapedala.frota.telemetria.api.TelemetriaApi;

@Service
public class ManutencaoService implements ManutencaoApi {
    private final Map<String, OrdemManutencao> ordens = new ConcurrentHashMap<>();

    public ManutencaoService(TelemetriaApi telemetria) {
        telemetria.subscreverAvarias(this::registarAvaria);
    }

    private void registarAvaria(AvariaDetetada avaria) {
        ordens.putIfAbsent(avaria.bicicletaId(),
                new OrdemManutencao(avaria.bicicletaId(), avaria.descricao(), avaria.ocorridoEm(), false));
    }

    @Override
    public Set<String> bicicletasForaDeServico() {
        return Set.copyOf(ordens.keySet());
    }

    public Set<OrdemManutencao> ordens() {
        return Set.copyOf(ordens.values());
    }
}
