package pt.lisboapedala.estacao.ocupacao.internal.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.estacao.ocupacao.api.OcupacaoEstacao;
import pt.lisboapedala.estacao.ocupacao.internal.Estacao;
import pt.lisboapedala.estacao.ocupacao.internal.OcupacaoService;

@RestController
@RequestMapping("/api/ocupacao/estacoes")
class OcupacaoController {

    private final OcupacaoService servico;

    OcupacaoController(OcupacaoService servico) {
        this.servico = servico;
    }

    @PostMapping
    ResponseEntity<OcupacaoResposta> registar(@RequestBody RegistoEstacaoPedido p) {
        var criada = servico.registarEstacao(new Estacao(p.id(), p.nome(), p.freguesia(), p.latitude(),
                p.longitude(), p.docas() == null ? List.of() : p.docas()));
        return ResponseEntity.created(URI.create("/api/ocupacao/estacoes/" + p.id())).body(paraResposta(criada));
    }

    @GetMapping
    List<OcupacaoResposta> listar() {
        return servico.todas().stream().map(OcupacaoController::paraResposta).toList();
    }

    @GetMapping("/{id}")
    ResponseEntity<OcupacaoResposta> obter(@PathVariable String id) {
        return ResponseEntity.of(servico.daEstacao(id).map(OcupacaoController::paraResposta));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail pedidoInvalido(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private static OcupacaoResposta paraResposta(OcupacaoEstacao o) {
        return new OcupacaoResposta(o.estacaoId(), o.nome(), o.freguesia(), o.latitude(), o.longitude(),
                o.capacidade(), o.docasOcupadas(), o.docasLivres(), o.atualizadoEm());
    }
}
