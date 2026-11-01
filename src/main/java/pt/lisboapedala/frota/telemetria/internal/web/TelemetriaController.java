package pt.lisboapedala.frota.telemetria.internal.web;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.lisboapedala.frota.telemetria.api.EstadoBicicletaReportado;
import pt.lisboapedala.frota.telemetria.internal.TelemetriaService;

@RestController
@RequestMapping("/api/frota/telemetria")
class TelemetriaController {
    private final TelemetriaService servico;

    TelemetriaController(TelemetriaService servico) {
        this.servico = servico;
    }

    @PostMapping
    void receber(@RequestBody EstadoBicicletaReportado estado) {
        servico.receber(estado);
    }

    @GetMapping("/{bicicletaId}")
    ResponseEntity<EstadoBicicletaReportado> estadoAtual(@PathVariable String bicicletaId) {
        Optional<EstadoBicicletaReportado> estado = servico.estadoAtual(bicicletaId);
        return ResponseEntity.of(estado);
    }
}
