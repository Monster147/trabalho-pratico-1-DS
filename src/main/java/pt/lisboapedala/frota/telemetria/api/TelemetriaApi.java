package pt.lisboapedala.frota.telemetria.api;

import java.util.Optional;
import java.util.function.Consumer;

public interface TelemetriaApi {
    Optional<EstadoBicicletaReportado> estadoAtual(String bicicletaId);

    void subscreverAvarias(Consumer<AvariaDetetada> consumidor);
}
