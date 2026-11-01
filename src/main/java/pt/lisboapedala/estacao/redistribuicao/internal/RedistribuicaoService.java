package pt.lisboapedala.estacao.redistribuicao.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pt.lisboapedala.estacao.ocupacao.api.Ocupacao;
import pt.lisboapedala.estacao.ocupacao.api.OcupacaoEstacao;
import pt.lisboapedala.estacao.redistribuicao.api.MovimentoSugerido;
import pt.lisboapedala.estacao.redistribuicao.api.PlanoRedistribuicao;
import pt.lisboapedala.estacao.redistribuicao.api.Redistribuicao;

/**
 * Heurística gulosa: estações abaixo do limiar mínimo recebem bicicletas da estação com excedente
 * mais próxima, até atingirem a ocupação-alvo. Limiares configuráveis.
 * Limitação: pela matriz não consulta manutencao, por isso conta também bicicletas fora de serviço.
 */
@Service
class RedistribuicaoService implements Redistribuicao {

    private final Ocupacao ocupacao;
    private final double limiarMinimo;
    private final double limiarMaximo;
    private final double alvo;

    RedistribuicaoService(Ocupacao ocupacao,
                          @Value("${lisboapedala.redistribuicao.limiar-minimo:0.2}") double limiarMinimo,
                          @Value("${lisboapedala.redistribuicao.limiar-maximo:0.8}") double limiarMaximo,
                          @Value("${lisboapedala.redistribuicao.ocupacao-alvo:0.5}") double alvo) {
        this.ocupacao = ocupacao;
        this.limiarMinimo = limiarMinimo;
        this.limiarMaximo = limiarMaximo;
        this.alvo = alvo;
    }

    @Override
    public PlanoRedistribuicao sugerirPlano(int capacidadeVeiculo) {
        if (capacidadeVeiculo <= 0) {
            throw new IllegalArgumentException("capacidadeVeiculo tem de ser positiva");
        }
        List<Saldo> excedentes = new ArrayList<>();
        List<Saldo> defices = new ArrayList<>();
        for (OcupacaoEstacao o : ocupacao.todas()) {
            if (o.capacidade() <= 0) {
                continue;
            }
            double taxa = (double) o.docasOcupadas() / o.capacidade();
            int objetivo = (int) Math.round(alvo * o.capacidade());
            if (taxa > limiarMaximo) {
                excedentes.add(new Saldo(o, o.docasOcupadas() - objetivo));
            } else if (taxa < limiarMinimo) {
                defices.add(new Saldo(o, objetivo - o.docasOcupadas()));
            }
        }
        defices.sort(Comparator.comparingInt((Saldo s) -> s.quantidade).reversed());

        List<MovimentoSugerido> movimentos = new ArrayList<>();
        for (Saldo destino : defices) {
            while (destino.quantidade > 0) {
                Saldo origem = maisProxima(destino.estacao, excedentes);
                if (origem == null) {
                    break;
                }
                int q = Math.min(capacidadeVeiculo, Math.min(destino.quantidade, origem.quantidade));
                movimentos.add(new MovimentoSugerido(origem.estacao.estacaoId(), destino.estacao.estacaoId(), q,
                        distancia(origem.estacao, destino.estacao)));
                origem.quantidade -= q;
                destino.quantidade -= q;
            }
        }
        return new PlanoRedistribuicao(movimentos, Instant.now());
    }

    private static Saldo maisProxima(OcupacaoEstacao destino, List<Saldo> excedentes) {
        return excedentes.stream()
                .filter(s -> s.quantidade > 0)
                .min(Comparator.comparingDouble(s -> distancia(s.estacao, destino)))
                .orElse(null);
    }

    private static double distancia(OcupacaoEstacao a, OcupacaoEstacao b) {
        return Distancias.km(a.latitude(), a.longitude(), b.latitude(), b.longitude());
    }

    private static final class Saldo {
        private final OcupacaoEstacao estacao;
        private int quantidade;

        private Saldo(OcupacaoEstacao estacao, int quantidade) {
            this.estacao = estacao;
            this.quantidade = quantidade;
        }
    }
}
