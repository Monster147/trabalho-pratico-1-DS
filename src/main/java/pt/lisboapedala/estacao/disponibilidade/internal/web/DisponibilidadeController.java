package pt.lisboapedala.estacao.disponibilidade.internal.web;

import java.time.Duration;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.lisboapedala.estacao.disponibilidade.api.Disponibilidade;
import pt.lisboapedala.estacao.disponibilidade.api.DisponibilidadeEstacao;

/** Consulta rápida do mapa pela App, com cache HTTP curta. */
@RestController
@RequestMapping("/api/disponibilidade")
class DisponibilidadeController {

    private static final CacheControl CACHE_CURTA = CacheControl.maxAge(Duration.ofSeconds(5)).cachePublic();

    private final Disponibilidade disponibilidade;

    DisponibilidadeController(Disponibilidade disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    @GetMapping("/mapa")
    ResponseEntity<List<EstacaoMapaResposta>> mapa() {
        var corpo = disponibilidade.mapa().stream().map(DisponibilidadeController::paraResposta).toList();
        return ResponseEntity.ok().cacheControl(CACHE_CURTA).body(corpo);
    }

    @GetMapping("/estacoes/{id}")
    ResponseEntity<EstacaoMapaResposta> estacao(@PathVariable String id) {
        return disponibilidade.daEstacao(id)
                .map(d -> ResponseEntity.ok().cacheControl(CACHE_CURTA).body(paraResposta(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static EstacaoMapaResposta paraResposta(DisponibilidadeEstacao d) {
        return new EstacaoMapaResposta(d.estacaoId(), d.nome(), d.latitude(), d.longitude(),
                d.bicicletasDisponiveis(), d.bicicletasBateriaBaixa(), d.docasLivres(), d.atualizadoEm());
    }
}
