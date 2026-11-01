package pt.lisboapedala.estacao.redistribuicao.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.estacao.redistribuicao.api.Redistribuicao;

@RestController
@RequestMapping("/api/redistribuicao")
class RedistribuicaoController {

    private final Redistribuicao redistribuicao;

    RedistribuicaoController(Redistribuicao redistribuicao) {
        this.redistribuicao = redistribuicao;
    }

    @GetMapping("/plano")
    PlanoResposta plano(@RequestParam(defaultValue = "20") int capacidadeVeiculo) {
        var plano = redistribuicao.sugerirPlano(capacidadeVeiculo);
        return new PlanoResposta(plano.movimentos().stream()
                .map(m -> new MovimentoResposta(m.origem(), m.destino(), m.quantidade(),
                        Math.round(m.distanciaKm() * 100) / 100.0))
                .toList(), plano.geradoEm());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail pedidoInvalido(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
