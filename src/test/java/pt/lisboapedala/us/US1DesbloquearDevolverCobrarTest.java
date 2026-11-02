package pt.lisboapedala.us;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import pt.lisboapedala.cobranca.faturacao.api.Fatura;
import pt.lisboapedala.cobranca.faturacao.api.FaturaEmitida;
import pt.lisboapedala.cobranca.faturacao.api.Faturacao;
import pt.lisboapedala.cobranca.pagamentos.api.Pagamentos;
import pt.lisboapedala.comum.Montante;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;
import pt.lisboapedala.viagem.viagens.api.Viagem;
import pt.lisboapedala.viagem.viagens.api.Viagens;

/**
 * US-1 (obrigatória): como assinante, desbloqueio e devolvo uma bicicleta e a viagem fica registada
 * e cobrada. Percorre identificacao → autorizacao → docas → viagens → ocupacao → tarifario →
 * faturacao → pagamentos, pela API REST, com a doca simulada a decidir o desbloqueio localmente.
 */
@SpringBootTest(properties = {
        "lisboapedala.tarifario.municipios.lisboa.minutos-gratis=45",
        "lisboapedala.tarifario.municipios.lisboa.bloco-minutos=15",
        "lisboapedala.tarifario.municipios.lisboa.preco-por-bloco-centimos=50",
        "lisboapedala.tarifario.municipios.lisboa.preco-base-ocasional-centimos=100",
        "lisboapedala.docas.diario=target/us1-${random.uuid}.log",
        "lisboapedala.autorizacao.sincronizacao.atraso-inicial-ms=10",
        "lisboapedala.autorizacao.sincronizacao.intervalo-ms=100"})
@AutoConfigureMockMvc
class US1DesbloquearDevolverCobrarTest {

    @TestConfiguration
    static class OuvintePagamentos {
        final List<FaturaEmitida> recebidas = new CopyOnWriteArrayList<>();

        /** Mesmo tipo de listener que pagamentos: prova que FaturaEmitida lhe chega. */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        void quandoFaturaEmitida(FaturaEmitida evento) {
            recebidas.add(evento);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Viagens viagens;
    @Autowired Faturacao faturacao;
    @Autowired Pagamentos pagamentos;
    @Autowired OuvintePagamentos ouvintePagamentos;

    @Test
    void assinanteDesbloqueiaDevolveEViagemFicaRegistadaECobrada() throws Exception {
        registarEstacao("MARQUES", "DMQ-1");
        registarEstacao("CAIS", "DCS-1");

        // 1. Registo do assinante (identificacao).
        JsonNode conta = json.readTree(mvc.perform(post("/api/identificacao/utilizadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Silva","email":"ana@exemplo.pt","nif":"123456789",
                                 "tipo":"ASSINANTE","numeroCartaoNavegante":"1234567890"}"""))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String utilizadorId = conta.get("id").asText();

        // 2. A App pede o QR assinado (autorizacao, K1).
        String qr = json.readTree(mvc.perform(post("/api/autorizacao/qr").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"utilizadorId\":\"" + utilizadorId + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("conteudo").asText();

        // 3. A doca de origem valida o QR sozinha, com o pacote sincronizado (ADR-001).
        var origem = new DocaSimulada(mvc, json, "DMQ-1");
        String chavePublica = origem.descarregarPacote().get("chavePublicaQr").asText();
        Instant inicio = Instant.now();
        String[] aceite = DocaSimulada.validarQr(qr, chavePublica, inicio);
        assertThat(aceite).as("a doca aceita o QR offline").isNotNull();
        assertThat(aceite[0]).isEqualTo(utilizadorId);
        assertThat(aceite[1]).isEqualTo("ASSINANTE");

        // 4. Desbloqueio e devolução 50 min depois noutra estação (docas, K3, ADR-003).
        assertThat(origem.enviarDesbloqueio(1, aceite[0], aceite[1], "B-101", inicio)).isEqualTo("ACEITE");
        String viagemId = "DMQ-1-1";
        var destino = new DocaSimulada(mvc, json, "DCS-1");
        assertThat(destino.enviarEncaixe(1, viagemId, inicio.plus(Duration.ofMinutes(50)))).isEqualTo("ACEITE");

        // 5. Viagem registada e fechada (viagens), com o tipo de utilizador certo.
        Viagem viagem = viagens.porId(viagemId);
        assertThat(viagem.terminadaEm()).isNotNull();
        assertThat(viagem.tipoUtilizador()).isEqualTo(TipoUtilizador.ASSINANTE);
        assertThat(viagem.duracao()).isEqualTo(Duration.ofMinutes(50));

        // 6. Ocupação atualizada na estação de destino (ocupacao).
        mvc.perform(get("/api/ocupacao/estacoes/CAIS")).andExpect(jsonPath("$.docasOcupadas").value(1));

        // 7. Cobrança assíncrona: fatura com o valor do tarifário (faturacao + tarifario).
        var id = new UtilizadorId(utilizadorId);
        List<Fatura> faturas = Esperar.ate(() -> faturacao.doUtilizador(id), l -> !l.isEmpty());
        assertThat(faturas).hasSize(1);
        assertThat(faturas.get(0).viagemId()).isEqualTo(viagemId);
        assertThat(faturas.get(0).nif()).isEqualTo("123456789");
        assertThat(faturas.get(0).montante()).isEqualTo(new Montante(50)); // 50 min → 1 bloco além dos 45 grátis

        // 8. FaturaEmitida chega a pagamentos, e o pagamento não deixou dívida.
        Esperar.ate(() -> ouvintePagamentos.recebidas, l -> !l.isEmpty());
        assertThat(ouvintePagamentos.recebidas).extracting(e -> e.fatura().viagemId()).contains(viagemId);
        Thread.sleep(200);
        assertThat(pagamentos.dividasDe(id)).isEmpty();
    }

    private void registarEstacao(String id, String doca) throws Exception {
        mvc.perform(post("/api/ocupacao/estacoes").contentType(MediaType.APPLICATION_JSON).content("""
                {"id":"%s","nome":"%s","freguesia":"Lisboa","latitude":38.72,"longitude":-9.14,"docas":["%s"]}"""
                .formatted(id, id, doca))).andExpect(status().isCreated());
    }
}
