package pt.lisboapedala.estacao.docas.api;

import java.time.Instant;
import java.util.Set;

/**
 * Dados que cada doca física descarrega para decidir o desbloqueio 100% localmente (ADR-001, K2).
 * Preparado por autorizacao e entregue às docas por este componente.
 *
 * @param versao                      cresce sempre; a doca só descarrega versões novas
 * @param algoritmoAssinatura         algoritmo das assinaturas dos QR (Ed25519)
 * @param chavePublicaQr              chave pública (X.509, Base64) para validar QR offline
 * @param cartoesNaveganteAutorizados cartões Navegante aceites
 * @param bicicletasForaDeServico     bicicletas que a doca não deve libertar
 */
public record PacoteAutorizacao(long versao, String algoritmoAssinatura, String chavePublicaQr,
                                Set<String> cartoesNaveganteAutorizados, Set<String> bicicletasForaDeServico,
                                Instant geradoEm) {

    public PacoteAutorizacao {
        cartoesNaveganteAutorizados = Set.copyOf(cartoesNaveganteAutorizados);
        bicicletasForaDeServico = Set.copyOf(bicicletasForaDeServico);
    }
}
