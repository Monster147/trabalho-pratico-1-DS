package pt.lisboapedala.estacao.disponibilidade.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import pt.lisboapedala.estacao.disponibilidade.api.DisponibilidadeEstacao;

/** Read-model em memória (thread-safe). Leituras O(1), sem I/O. */
@Component
class VistaMapaCache {

    private final Map<String, DisponibilidadeEstacao> porEstacao = new ConcurrentHashMap<>();
    private final Map<String, Integer> bateriaPorBicicleta = new ConcurrentHashMap<>();

    Collection<DisponibilidadeEstacao> todas() {
        return porEstacao.values();
    }

    Optional<DisponibilidadeEstacao> daEstacao(String id) {
        return Optional.ofNullable(porEstacao.get(id));
    }

    void guardar(DisponibilidadeEstacao d) {
        porEstacao.put(d.estacaoId(), d);
    }

    void manterApenas(Set<String> existentes) {
        porEstacao.keySet().retainAll(existentes);
    }

    void registarBateria(String bicicleta, int percentagem) {
        bateriaPorBicicleta.put(bicicleta, percentagem);
    }

    Optional<Integer> bateria(String bicicleta) {
        return Optional.ofNullable(bateriaPorBicicleta.get(bicicleta));
    }
}
