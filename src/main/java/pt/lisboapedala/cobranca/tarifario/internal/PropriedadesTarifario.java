package pt.lisboapedala.cobranca.tarifario.internal;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tarifários por município, lidos de {@code application.properties} (ADR-002, C3).
 * Exemplo: {@code lisboapedala.tarifario.municipios.lisboa.minutos-gratis=45}.
 * Uma câmara nova ou uma mudança de preço só altera esta configuração, não o código.
 */
@ConfigurationProperties(prefix = "lisboapedala.tarifario")
record PropriedadesTarifario(Map<String, Regras> municipios) {

    PropriedadesTarifario {
        municipios = municipios == null ? Map.of() : Map.copyOf(municipios);
    }

    /** Valores em cêntimos de euro. */
    record Regras(long minutosGratis, long blocoMinutos, long precoPorBlocoCentimos,
                  long precoBaseOcasionalCentimos) {
    }
}
