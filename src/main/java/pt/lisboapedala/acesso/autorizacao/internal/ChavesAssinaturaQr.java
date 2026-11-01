package pt.lisboapedala.acesso.autorizacao.internal;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * Par Ed25519 para assinar QR. A privada nunca sai do backend; a pública vai para as docas.
 * Simplificação: gerado no arranque. Em produção deve vir de keystore/HSM, com rotação.
 */
@Component
class ChavesAssinaturaQr {

    static final String ALGORITMO = "Ed25519";

    private final KeyPair par;

    ChavesAssinaturaQr() throws GeneralSecurityException {
        this.par = KeyPairGenerator.getInstance(ALGORITMO).generateKeyPair();
    }

    PrivateKey privada() {
        return par.getPrivate();
    }

    /** Chave pública X.509 (SubjectPublicKeyInfo) em Base64. */
    String chavePublicaBase64() {
        return Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
    }
}
