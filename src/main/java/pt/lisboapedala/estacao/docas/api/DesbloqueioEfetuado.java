package pt.lisboapedala.estacao.docas.api;

import java.time.Instant;
import pt.lisboapedala.comum.DocaId;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/**
 * Desbloqueio decidido localmente pela doca (ADR-001) e já deduplicado por docas (ADR-003).
 * O tipo de utilizador é indicado pela doca: vem no QR assinado, ou é ASSINANTE quando o
 * desbloqueio é feito com cartão Navegante. Sem ele, viagens não saberia que tarifa aplicar.
 */
public record DesbloqueioEfetuado(
        DocaId docaId, long numeroSequencia, UtilizadorId utilizadorId,
        String bicicletaId, MunicipioId municipioId, TipoUtilizador tipoUtilizador, Instant ocorridoEm) {
    public String viagemId() {
        return docaId.valor() + "-" + numeroSequencia;
    }
}
