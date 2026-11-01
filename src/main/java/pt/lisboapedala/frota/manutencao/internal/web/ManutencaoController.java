package pt.lisboapedala.frota.manutencao.internal.web;

import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.lisboapedala.frota.manutencao.api.OrdemManutencao;
import pt.lisboapedala.frota.manutencao.internal.ManutencaoService;

@RestController
@RequestMapping("/api/frota/manutencao")
class ManutencaoController {
    private final ManutencaoService servico;

    ManutencaoController(ManutencaoService servico) {
        this.servico = servico;
    }

    @GetMapping("/ordens")
    Set<OrdemManutencao> ordens() {
        return servico.ordens();
    }
}
