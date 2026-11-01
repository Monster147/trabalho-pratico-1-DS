package pt.lisboapedala.acesso.identificacao.internal.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.acesso.identificacao.internal.Utilizador;
import pt.lisboapedala.acesso.identificacao.internal.UtilizadoresService;
import pt.lisboapedala.comum.UtilizadorId;

/** Endpoints REST de identificacao. A autenticação (JWT/OAuth2) fica fora do âmbito. */
@RestController
@RequestMapping("/api/identificacao/utilizadores")
class IdentificacaoController {

    private final UtilizadoresService utilizadores;

    IdentificacaoController(UtilizadoresService utilizadores) {
        this.utilizadores = utilizadores;
    }

    @PostMapping
    ResponseEntity<UtilizadorResposta> registar(@RequestBody RegistoUtilizadorPedido p) {
        Utilizador u = utilizadores.registar(p.nome(), p.email(), p.nif(), p.tipo(), p.numeroCartaoNavegante());
        return ResponseEntity.created(URI.create("/api/identificacao/utilizadores/" + u.id().valor()))
                .body(paraResposta(u));
    }

    @GetMapping("/{id}")
    ResponseEntity<UtilizadorResposta> obter(@PathVariable String id) {
        return ResponseEntity.of(utilizadores.procurar(new UtilizadorId(id)).map(IdentificacaoController::paraResposta));
    }

    @GetMapping("/{id}/elegibilidade")
    ElegibilidadeResposta elegibilidade(@PathVariable String id) {
        var e = utilizadores.verificar(new UtilizadorId(id));
        return new ElegibilidadeResposta(id, e.elegivel(), e.motivo().name());
    }

    @PutMapping("/{id}/estado")
    UtilizadorResposta alterarEstado(@PathVariable String id, @RequestBody AlterarEstadoPedido p) {
        if (p.estado() == null) {
            throw new IllegalArgumentException("estado obrigatório");
        }
        return paraResposta(utilizadores.alterarEstado(new UtilizadorId(id), p.estado()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail pedidoInvalido(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private static UtilizadorResposta paraResposta(Utilizador u) {
        return new UtilizadorResposta(u.id().valor(), u.nome(), u.email(), u.tipo().name(), u.estado().name());
    }
}
