package pt.lisboapedala.acesso.identificacao.internal;

import org.springframework.stereotype.Component;

/** Simulação do Sistema Navegante: um passe é válido se tiver 10 dígitos. */
@Component
class ValidadorNaveganteSimulado implements ValidadorNavegante {

    @Override
    public boolean passeValido(String numeroCartao) {
        return numeroCartao != null && numeroCartao.matches("\\d{10}");
    }
}
