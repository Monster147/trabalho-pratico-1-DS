package pt.lisboapedala.us;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * US-2: como técnico, recebo uma ordem de manutenção quando uma bicicleta reporta avaria.
 * Percorre telemetria → manutencao → autorizacao: a bicicleta avariada passa a constar do pacote
 * sincronizado com as docas, que deixam de a libertar.
 */
@SpringBootTest(properties = {
        "lisboapedala.docas.diario=target/us2-${random.uuid}.log",
        "lisboapedala.autorizacao.sincronizacao.atraso-inicial-ms=10",
        "lisboapedala.autorizacao.sincronizacao.intervalo-ms=100"})
@AutoConfigureMockMvc
class US2AvariaGeraOrdemManutencaoTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void bicicletaComBateriaBaixaGeraOrdemEFicaForaDeServicoNasDocas() throws Exception {
        // 1. A bicicleta envia telemetria com a bateria a 10 % (telemetria).
        mvc.perform(post("/api/frota/telemetria").contentType(MediaType.APPLICATION_JSON).content("""
                {"bicicletaId":"B-777","bateriaPercentagem":10,"latitude":38.71,"longitude":-9.14,
                 "ocorridoEm":"%s"}""".formatted(Instant.now()))).andExpect(status().is2xxSuccessful());

        // 2. O técnico vê a ordem de manutenção (manutencao).
        mvc.perform(get("/api/frota/manutencao/ordens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].bicicletaId", Matchers.hasItem("B-777")));

        // 3. A bicicleta entra no pacote sincronizado com as docas (autorizacao, K2).
        var doca = new DocaSimulada(mvc, json, "DOCA-US2");
        var pacote = doca.esperarPacote(p -> p.get("bicicletasForaDeServico").toString().contains("B-777"));
        assertThat(pacote.get("bicicletasForaDeServico").toString()).contains("B-777");
    }
}
