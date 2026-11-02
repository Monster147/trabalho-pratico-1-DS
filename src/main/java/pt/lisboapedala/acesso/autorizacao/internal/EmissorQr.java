package pt.lisboapedala.acesso.autorizacao.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import pt.lisboapedala.acesso.autorizacao.api.QrAssinado;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/** Gera QR assinados com validade curta (configurável). */
@Component
class EmissorQr {

    static final String PREFIXO = "LP1";
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

    private final ChavesAssinaturaQr chaves;
    private final Clock relogio;
    private final Duration validade;

    @Autowired
    EmissorQr(ChavesAssinaturaQr chaves, @Value("${lisboapedala.autorizacao.qr-validade:PT5M}") Duration validade) {
        this(chaves, Clock.systemUTC(), validade);
    }

    EmissorQr(ChavesAssinaturaQr chaves, Clock relogio, Duration validade) {
        this.chaves = chaves;
        this.relogio = relogio;
        this.validade = validade;
    }

    QrAssinado emitir(UtilizadorId utilizadorId, TipoUtilizador tipo) {
        Instant emitidoEm = relogio.instant();
        Instant expiraEm = emitidoEm.plus(validade);
        String payload = String.join("|", utilizadorId.valor(), tipo.name(), Long.toString(emitidoEm.getEpochSecond()),
                Long.toString(expiraEm.getEpochSecond()), UUID.randomUUID().toString());
        String mensagem = PREFIXO + "." + B64URL.encodeToString(payload.getBytes(UTF_8));
        return new QrAssinado(mensagem + "." + B64URL.encodeToString(assinar(mensagem)), expiraEm);
    }

    private byte[] assinar(String mensagem) {
        try {
            Signature assinatura = Signature.getInstance(ChavesAssinaturaQr.ALGORITMO);
            assinatura.initSign(chaves.privada());
            assinatura.update(mensagem.getBytes(UTF_8));
            return assinatura.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar QR", e);
        }
    }
}
