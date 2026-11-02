package pt.lisboapedala.us;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import pt.lisboapedala.cobranca.faturacao.api.Faturacao;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.viagem.viagens.api.Viagens;

/**
 * US-3: como doca sem rede, reenvio os eventos guardados quando a rede volta, sem duplicar viagens.
 * Percorre docas → viagens → ocupacao (e a cobrança, para provar que também não há fatura a dobrar).
 */
@SpringBootTest(properties = {
        "lisboapedala.tarifario.municipios.lisboa.minutos-gratis=45",
        "lisboapedala.tarifario.municipios.lisboa.bloco-minutos=15",
        "lisboapedala.tarifario.municipios.lisboa.preco-por-bloco-centimos=50",
        "lisboapedala.tarifario.municipios.lisboa.preco-base-ocasional-centimos=100",
        "lisboapedala.docas.diario=target/us3-${random.uuid}.log",
        "lisboapedala.autorizacao.sincronizacao.atraso-inicial-ms=10"})
@AutoConfigureMockMvc
class US3ReenvioSemDuplicarTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Viagens viagens;
    @Autowired Faturacao faturacao;

    @Test
    void reenviosEEventosForaDeOrdemNaoDuplicamViagensNemFaturas() throws Exception {
        String utilizadorId = json.readTree(mvc.perform(post("/api/identificacao/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Rui Costa","email":"rui@exemplo.pt","nif":"123456789",
                                 "tipo":"ASSINANTE","numeroCartaoNavegante":"0987654321"}"""))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        var origem = new DocaSimulada(mvc, json, "DOCA-SEM-REDE");
        var destino = new DocaSimulada(mvc, json, "DOCA-DESTINO");
        Instant inicio = Instant.now().minus(Duration.ofHours(2));
        String viagemId = "DOCA-SEM-REDE-1";

        // A doca de origem estava sem rede: o encaixe no destino chega primeiro.
        assertThat(destino.enviarEncaixe(1, viagemId, inicio.plus(Duration.ofMinutes(20)))).isEqualTo("ACEITE");

        // A rede volta: a doca de origem envia o desbloqueio guardado...
        assertThat(origem.enviarDesbloqueio(1, utilizadorId, "ASSINANTE", "B-300", inicio)).isEqualTo("ACEITE");
        // ...e, como não recebeu o ACK a tempo, volta a enviá-lo. O destino também reenvia.
        assertThat(origem.enviarDesbloqueio(1, utilizadorId, "ASSINANTE", "B-300", inicio)).isEqualTo("DUPLICADO");
        assertThat(destino.enviarEncaixe(1, viagemId, inicio.plus(Duration.ofMinutes(20)))).isEqualTo("DUPLICADO");

        // Uma só viagem, fechada.
        assertThat(viagens.todas()).filteredOn(v -> v.id().equals(viagemId)).hasSize(1);
        assertThat(viagens.porId(viagemId).terminadaEm()).isNotNull();

        // Uma só fatura.
        var id = new UtilizadorId(utilizadorId);
        Esperar.ate(() -> faturacao.doUtilizador(id), l -> !l.isEmpty());
        Thread.sleep(300);
        assertThat(faturacao.doUtilizador(id)).hasSize(1);
    }
}
