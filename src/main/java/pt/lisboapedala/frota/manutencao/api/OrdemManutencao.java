package pt.lisboapedala.frota.manutencao.api;

import java.time.Instant;

public record OrdemManutencao(String bicicletaId, String motivo, Instant criadaEm, boolean concluida) {
}
