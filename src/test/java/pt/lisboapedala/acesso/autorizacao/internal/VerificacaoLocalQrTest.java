package pt.lisboapedala.acesso.autorizacao.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/** ADR-001: a doca valida o QR offline, só com a chave pública do pacote de sincronização. */
class VerificacaoLocalQrTest {

    private final Instant agora = Instant.parse("2026-11-01T10:00:00Z");

    @Test
    void docaValidaQrSoComAChavePublica() throws Exception {
        var chaves = new ChavesAssinaturaQr();
        var emissor = new EmissorQr(chaves, Clock.fixed(agora, ZoneOffset.UTC), Duration.ofMinutes(5));
        String qr = emissor.emitir(new UtilizadorId("U1"), TipoUtilizador.ASSINANTE).conteudo();

        assertThat(docaAceita(qr, chaves.chavePublicaBase64(), agora.plusSeconds(60))).isTrue();
        assertThat(docaAceita(qr, chaves.chavePublicaBase64(), agora.plusSeconds(600))).isFalse();
    }

    @Test
    void qrAdulteradoERejeitado() throws Exception {
        var chaves = new ChavesAssinaturaQr();
        var emissor = new EmissorQr(chaves, Clock.fixed(agora, ZoneOffset.UTC), Duration.ofMinutes(5));
        String[] partes = emissor.emitir(new UtilizadorId("U1"), TipoUtilizador.OCASIONAL).conteudo().split("\\.");
        String falso = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("U1|ASSINANTE|0|9999999999|x".getBytes(UTF_8));

        assertThat(docaAceita(partes[0] + "." + falso + "." + partes[2], chaves.chavePublicaBase64(), agora)).isFalse();
    }

    /** Lógica de referência para o firmware da doca. */
    private static boolean docaAceita(String qr, String chavePublica, Instant relogioDaDoca) throws Exception {
        String[] p = qr.split("\\.");
        if (p.length != 3 || !EmissorQr.PREFIXO.equals(p[0])) {
            return false;
        }
        PublicKey chave = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(chavePublica)));
        Signature v = Signature.getInstance("Ed25519");
        v.initVerify(chave);
        v.update((p[0] + "." + p[1]).getBytes(UTF_8));
        if (!v.verify(Base64.getUrlDecoder().decode(p[2]))) {
            return false;
        }
        String[] campos = new String(Base64.getUrlDecoder().decode(p[1]), UTF_8).split("\\|");
        return relogioDaDoca.getEpochSecond() <= Long.parseLong(campos[3]);
    }
}
