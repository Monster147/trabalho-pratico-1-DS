package pt.lisboapedala.cobranca.tarifario.api;

import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.MunicipioId;

public record ConfiguracaoTarifario(MunicipioId municipioId, long minutosGratis,
                                    long blocoMinutos, Montante precoPorBloco,
                                    Montante precoBaseOcasional) {
    public ConfiguracaoTarifario {
        if (minutosGratis < 0 || blocoMinutos <= 0) throw new IllegalArgumentException("Configuração inválida");
    }
}
