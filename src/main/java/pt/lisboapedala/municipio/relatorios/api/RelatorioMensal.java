package pt.lisboapedala.municipio.relatorios.api;

import java.time.YearMonth;

public record RelatorioMensal(YearMonth mes, String freguesia, long viagensConcluidas,
                              long minutosUtilizacao, long bicicletasDisponiveis,
                              double co2EvitadoKg) {
}
