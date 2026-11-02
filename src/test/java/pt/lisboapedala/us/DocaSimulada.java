package pt.lisboapedala.us;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Simula o software de uma doca física nos testes das user stories: descarrega o pacote de
 * sincronização (K2), valida o QR localmente só com a chave pública (ADR-001) e envia os
 * eventos ao backend (K3), tal como a doca real faria.
 */
final class DocaSimulada {

    private final MockMvc mvc;
    private final ObjectMapper json;
    private final String docaId;

    DocaSimulada(MockMvc mvc, ObjectMapper json, String docaId) {
        this.mvc = mvc;
        this.json = json;
        this.docaId = docaId;
    }

    /** Espera pelo primeiro pacote de sincronização (gerado em background por autorizacao). */
    JsonNode descarregarPacote() throws Exception {
        return esperarPacote(p -> true);
    }

    JsonNode esperarPacote(java.util.function.Predicate<JsonNode> condicao) throws Exception {
        long limite = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < limite) {
            MvcResult r = mvc.perform(get("/api/docas/" + docaId + "/sincronizacao")).andReturn();
            if (r.getResponse().getStatus() == 200) {
                JsonNode pacote = json.readTree(r.getResponse().getContentAsString());
                if (condicao.test(pacote)) {
                    return pacote;
                }
            }
            Thread.sleep(50);
        }
        throw new AssertionError("A doca " + docaId + " não recebeu o pacote esperado");
    }

    /**
     * Decisão local de desbloqueio (ADR-001): verifica a assinatura e a validade sem chamar o backend.
     *
     * @return {utilizadorId, tipoUtilizador}, ou null se o QR for recusado
     */
    static String[] validarQr(String qr, String chavePublica, Instant relogioDaDoca) throws Exception {
        String[] partes = qr.split("\\.");
        if (partes.length != 3 || !"LP1".equals(partes[0])) {
            return null;
        }
        var chave = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(chavePublica)));
        var verificador = Signature.getInstance("Ed25519");
        verificador.initVerify(chave);
        verificador.update((partes[0] + "." + partes[1]).getBytes(UTF_8));
        if (!verificador.verify(Base64.getUrlDecoder().decode(partes[2]))) {
            return null;
        }
        String[] campos = new String(Base64.getUrlDecoder().decode(partes[1]), UTF_8).split("\\|");
        if (relogioDaDoca.getEpochSecond() > Long.parseLong(campos[3])) {
            return null;
        }
        return new String[] {campos[0], campos[1]};
    }

    String enviarDesbloqueio(long seq, String utilizadorId, String tipo, String bicicletaId, Instant em)
            throws Exception {
        return enviar(String.format("""
                {"tipo":"DESBLOQUEIO_EFETUADO","numeroSequencia":%d,"utilizadorId":"%s","tipoUtilizador":"%s",
                 "bicicletaId":"%s","municipioId":"lisboa","ocorridoEm":"%s"}""", seq, utilizadorId, tipo, bicicletaId, em));
    }

    String enviarEncaixe(long seq, String viagemId, Instant em) throws Exception {
        return enviar(String.format("""
                {"tipo":"BICICLETA_ENCAIXADA","numeroSequencia":%d,"viagemId":"%s","ocorridoEm":"%s"}""",
                seq, viagemId, em));
    }

    /** Envia um evento e devolve o resultado do ACK (ACEITE, ACEITE_AGUARDA_ORDEM ou DUPLICADO). */
    private String enviar(String corpo) throws Exception {
        MvcResult r = mvc.perform(post("/api/docas/" + docaId + "/eventos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
        if (r.getResponse().getStatus() != 200) {
            throw new AssertionError("Sem ACK: " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        }
        return json.readTree(r.getResponse().getContentAsString()).get("resultado").asText();
    }
}
