package pt.lisboapedala.estacao.docas.api;

import java.time.Instant;
import pt.lisboapedala.comum.DocaId;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.UtilizadorId;

public record DesbloqueioEfetuado(
        DocaId docaId, long numeroSequencia, UtilizadorId utilizadorId,
        String bicicletaId, MunicipioId municipioId, Instant ocorridoEm) {
    public String viagemId() {
        return docaId.valor() + "-" + numeroSequencia;
    }
}
