package pt.lisboapedala.municipio.relatorios.api;

import java.time.YearMonth;

public interface RelatoriosApi {
    RelatorioMensal gerar(YearMonth mes, String freguesia);
}
