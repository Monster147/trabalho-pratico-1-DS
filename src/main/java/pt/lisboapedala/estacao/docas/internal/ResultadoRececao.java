package pt.lisboapedala.estacao.docas.internal;

public enum ResultadoRececao {
    /** Persistido e publicado. */
    ACEITE,
    /** Persistido; publicado quando chegarem os números de sequência em falta. */
    ACEITE_AGUARDA_ORDEM,
    /** Chave (docaId, numeroSequencia) já conhecida: não volta a ser publicado. */
    DUPLICADO
}
