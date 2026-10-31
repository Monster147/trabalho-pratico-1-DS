package pt.lisboapedala.viagem.viagens.api;

import java.time.Duration;
import java.time.Instant;
import pt.lisboapedala.comum.MunicipioId;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

public record Viagem(String id, UtilizadorId utilizadorId, String bicicletaId,
                     MunicipioId municipioId, TipoUtilizador tipoUtilizador,
                     Instant iniciadaEm, Instant terminadaEm) {
    public Duration duracao() {
        if (terminadaEm == null) throw new IllegalStateException("A viagem ainda está aberta");
        return Duration.between(iniciadaEm, terminadaEm);
    }
}
