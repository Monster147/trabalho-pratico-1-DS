package pt.lisboapedala.acesso.identificacao.internal;

/** Porta para o sistema externo Navegante (validação de passes). */
interface ValidadorNavegante {

    boolean passeValido(String numeroCartao);
}
