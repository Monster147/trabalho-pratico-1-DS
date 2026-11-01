package pt.lisboapedala.estacao.docas.api;

/**
 * Canal backend → docas físicas (K2). As docas descarregam o pacote mais recente quando têm rede.
 * Usado por: autorizacao.
 */
public interface SincronizacaoDocas {

    /** Pacotes com versão igual ou inferior à atual são ignorados. */
    void disponibilizar(PacoteAutorizacao pacote);
}
