package pt.lisboapedala.viagem.historico.api;

import pt.lisboapedala.cobranca.faturacao.api.Fatura;
import pt.lisboapedala.viagem.viagens.api.Viagem;

public record HistoricoItem(Viagem viagem, Fatura fatura) {
}
