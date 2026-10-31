package pt.lisboapedala.viagem.historico.internal;

import java.util.List;
import org.springframework.stereotype.Service;
import pt.lisboapedala.cobranca.faturacao.api.Faturacao;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.viagem.historico.api.Historico;
import pt.lisboapedala.viagem.historico.api.HistoricoItem;
import pt.lisboapedala.viagem.viagens.api.Viagens;

@Service
public class HistoricoService implements Historico {
    private final Viagens viagens;
    private final Faturacao faturacao;

    public HistoricoService(Viagens viagens, Faturacao faturacao) {
        this.viagens = viagens;
        this.faturacao = faturacao;
    }

    @Override
    public List<HistoricoItem> consultar(UtilizadorId utilizadorId) {
        var faturas = faturacao.doUtilizador(utilizadorId);
        return viagens.doUtilizador(utilizadorId).stream()
                .map(viagem -> faturas.stream()
                        .filter(fatura -> fatura.viagemId().equals(viagem.id()))
                        .findFirst()
                        .map(fatura -> new HistoricoItem(viagem, fatura))
                        .orElseGet(() -> new HistoricoItem(viagem, null)))
                .toList();
    }
}
