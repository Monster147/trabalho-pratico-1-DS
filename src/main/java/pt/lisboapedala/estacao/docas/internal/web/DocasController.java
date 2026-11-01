package pt.lisboapedala.estacao.docas.internal.web;

import java.io.UncheckedIOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.estacao.docas.api.PacoteAutorizacao;
import pt.lisboapedala.estacao.docas.internal.EventoDocaRecebido;
import pt.lisboapedala.estacao.docas.internal.RececaoEventosService;
import pt.lisboapedala.estacao.docas.internal.SincronizacaoDocasService;

/** Interface HTTP das docas físicas: K3 (eventos) e K2 (sincronização). */
@RestController
@RequestMapping("/api/docas")
class DocasController {

    private final RececaoEventosService rececao;
    private final SincronizacaoDocasService sincronizacao;

    DocasController(RececaoEventosService rececao, SincronizacaoDocasService sincronizacao) {
        this.rececao = rececao;
        this.sincronizacao = sincronizacao;
    }

    /**
     * Só responde com ACK depois de a chave (docaId, numeroSequencia) estar persistida.
     * Sem ACK (5xx), a doca reenvia; o reenvio é seguro por ser idempotente.
     */
    @PostMapping("/{docaId}/eventos")
    AckResposta receberEvento(@PathVariable String docaId, @RequestBody EventoDocaPedido p) {
        var evento = new EventoDocaRecebido(docaId, p.numeroSequencia(), p.tipo(), p.utilizadorId(),
                p.bicicletaId(), p.municipioId(), p.viagemId(), p.ocorridoEm());
        var resultado = rececao.receber(evento);
        return new AckResposta(docaId, p.numeroSequencia(), resultado.name());
    }

    /** A doca envia a versão que já tem; recebe 304 se estiver atualizada, 204 se ainda não houver pacote. */
    @GetMapping("/{docaId}/sincronizacao")
    ResponseEntity<PacoteAutorizacao> sincronizar(@PathVariable String docaId,
                                                  @RequestParam(defaultValue = "-1") long versaoAtual) {
        return sincronizacao.pacoteAtual()
                .map(pacote -> pacote.versao() > versaoAtual
                        ? ResponseEntity.ok(pacote)
                        : ResponseEntity.status(HttpStatus.NOT_MODIFIED).<PacoteAutorizacao>build())
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail pedidoInvalido(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UncheckedIOException.class)
    ProblemDetail semAck(UncheckedIOException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Evento não persistido; reenviar mais tarde");
    }
}
