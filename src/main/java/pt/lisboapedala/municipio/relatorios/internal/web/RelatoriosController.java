package pt.lisboapedala.municipio.relatorios.internal.web;

import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.lisboapedala.municipio.relatorios.api.RelatorioMensal;
import pt.lisboapedala.municipio.relatorios.internal.RelatoriosService;

@RestController
@RequestMapping("/api/municipios/relatorios")
class RelatoriosController {
    private final RelatoriosService servico;

    RelatoriosController(RelatoriosService servico) {
        this.servico = servico;
    }

    @GetMapping("/mensal")
    RelatorioMensal mensal(@RequestParam YearMonth mes, @RequestParam String freguesia) {
        return servico.gerar(mes, freguesia);
    }
}
