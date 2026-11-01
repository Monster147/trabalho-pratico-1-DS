package pt.lisboapedala.acesso.autorizacao.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.acesso.autorizacao.api.Autorizacao;
import pt.lisboapedala.acesso.autorizacao.api.UtilizadorNaoElegivelException;
import pt.lisboapedala.comum.UtilizadorId;

/** K1: a App pede um QR assinado (HTTP REST síncrono). */
@RestController
@RequestMapping("/api/autorizacao")
class AutorizacaoController {

    private final Autorizacao autorizacao;

    AutorizacaoController(Autorizacao autorizacao) {
        this.autorizacao = autorizacao;
    }

    @PostMapping("/qr")
    QrResposta emitirQr(@RequestBody PedidoQr pedido) {
        if (pedido.utilizadorId() == null || pedido.utilizadorId().isBlank()) {
            throw new IllegalArgumentException("utilizadorId é obrigatório");
        }
        var qr = autorizacao.emitirQr(new UtilizadorId(pedido.utilizadorId()));
        return new QrResposta(qr.conteudo(), qr.expiraEm());
    }

    @ExceptionHandler(UtilizadorNaoElegivelException.class)
    ProblemDetail naoElegivel(UtilizadorNaoElegivelException ex) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problema.setProperty("motivo", ex.getMotivo());
        return problema;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail pedidoInvalido(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
