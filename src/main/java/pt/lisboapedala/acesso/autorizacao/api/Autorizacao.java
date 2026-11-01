package pt.lisboapedala.acesso.autorizacao.api;

import pt.lisboapedala.comum.UtilizadorId;

/** Interface pública do componente autorizacao (emissão de QR assinados, K1). */
public interface Autorizacao {

    /**
     * Emite um QR assinado que a doca valida offline com a chave pública sincronizada (ADR-001).
     *
     * @throws UtilizadorNaoElegivelException se o utilizador não puder desbloquear bicicletas
     */
    QrAssinado emitirQr(UtilizadorId utilizadorId);
}
