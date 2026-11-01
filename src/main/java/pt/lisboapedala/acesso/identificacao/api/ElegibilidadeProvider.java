package pt.lisboapedala.acesso.identificacao.api;

import java.util.List;

import pt.lisboapedala.comum.UtilizadorId;

/** Elegibilidade para desbloquear bicicletas. Usado por: autorizacao. */
public interface ElegibilidadeProvider {

    /** Conta tem de existir e estar ativa; se for ASSINANTE, o passe Navegante tem de ser válido. */
    Elegibilidade verificar(UtilizadorId utilizadorId);

    /** Cartões Navegante aceites, para sincronizar com as docas (desbloqueio local, ADR-001). */
    List<CartaoNaveganteElegivel> cartoesNaveganteElegiveis();
}
