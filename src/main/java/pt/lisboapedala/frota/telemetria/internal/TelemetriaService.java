package pt.lisboapedala.frota.telemetria.internal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import pt.lisboapedala.frota.telemetria.api.AvariaDetetada;
import pt.lisboapedala.frota.telemetria.api.EstadoBicicletaReportado;
import pt.lisboapedala.frota.telemetria.api.TelemetriaApi;

@Service
public class TelemetriaService implements TelemetriaApi {
    private static final int LIMIAR_BATERIA_BAIXA = 15;

    private final ConcurrentMap<String, EstadoBicicletaReportado> estados = new ConcurrentHashMap<>();
    private final List<Consumer<AvariaDetetada>> consumidores = new CopyOnWriteArrayList<>();

    public void receber(EstadoBicicletaReportado estado) {
        estados.compute(estado.bicicletaId(), (id, anterior) ->
                anterior == null || !anterior.ocorridoEm().isAfter(estado.ocorridoEm()) ? estado : anterior);
        if (estado.bateriaPercentagem() <= LIMIAR_BATERIA_BAIXA) {
            notificar(new AvariaDetetada(estado.bicicletaId(), "Bateria baixa", estado.ocorridoEm()));
        }
    }

    @Override
    public Optional<EstadoBicicletaReportado> estadoAtual(String bicicletaId) {
        return Optional.ofNullable(estados.get(bicicletaId));
    }

    @Override
    public void subscreverAvarias(Consumer<AvariaDetetada> consumidor) {
        consumidores.add(consumidor);
    }

    private void notificar(AvariaDetetada avaria) {
        consumidores.forEach(consumidor -> consumidor.accept(avaria));
    }
}
