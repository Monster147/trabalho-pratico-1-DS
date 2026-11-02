package pt.lisboapedala.estacao.docas.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationEventPublisher;

import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.estacao.docas.api.BicicletaEncaixada;
import pt.lisboapedala.estacao.docas.api.DesbloqueioEfetuado;

/** US-3 / ADR-003 (unitário): idempotência, ordem por doca e persistência que sobrevive a reinícios. */
class RececaoIdempotenteTest {

    @TempDir
    Path pasta;

    private final List<Object> publicados = new ArrayList<>();
    private final ApplicationEventPublisher publicador = publicados::add;

    @Test
    void reenvioDoMesmoEventoNaoEPublicadoDuasVezes() {
        var rececao = new RececaoEventosService(new DiarioEventosDocas(pasta.resolve("d.log")), publicador);
        var e = desbloqueio("D1", 1);

        assertThat(rececao.receber(e)).isEqualTo(ResultadoRececao.ACEITE);
        assertThat(rececao.receber(e)).isEqualTo(ResultadoRececao.DUPLICADO);
        assertThat(publicados).hasSize(1).first().isInstanceOf(DesbloqueioEfetuado.class);
    }

    @Test
    void eventosForaDeOrdemSaoPublicadosPorOrdem() {
        var rececao = new RececaoEventosService(new DiarioEventosDocas(pasta.resolve("d.log")), publicador);

        assertThat(rececao.receber(encaixe("D2", 1))).isEqualTo(ResultadoRececao.ACEITE);
        assertThat(rececao.receber(encaixe("D2", 3))).isEqualTo(ResultadoRececao.ACEITE_AGUARDA_ORDEM);
        assertThat(rececao.receber(encaixe("D2", 2))).isEqualTo(ResultadoRececao.ACEITE);

        assertThat(publicados).map(o -> ((BicicletaEncaixada) o).numeroSequencia()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void chaveSobreviveAReinicioEPendentesSaoRetomados() {
        Path ficheiro = pasta.resolve("d.log");
        var antes = new RececaoEventosService(new DiarioEventosDocas(ficheiro), publicador);
        antes.receber(encaixe("D3", 1));
        antes.receber(encaixe("D3", 3)); // fica pendente (falta o 2)

        // "Reinício": novo diário a partir do mesmo ficheiro.
        publicados.clear();
        var depois = new RececaoEventosService(new DiarioEventosDocas(ficheiro), publicador);

        assertThat(depois.receber(encaixe("D3", 1))).isEqualTo(ResultadoRececao.DUPLICADO);
        assertThat(depois.receber(encaixe("D3", 2))).isEqualTo(ResultadoRececao.ACEITE);
        assertThat(publicados).map(o -> ((BicicletaEncaixada) o).numeroSequencia()).containsExactly(2L, 3L);
    }

    @Test
    void tipoDeUtilizadorSobreviveAoReinicio() {
        Path ficheiro = pasta.resolve("d.log");
        new RececaoEventosService(new DiarioEventosDocas(ficheiro), publicador).receber(desbloqueio("D4", 1));
        new RececaoEventosService(new DiarioEventosDocas(ficheiro), publicador).receber(desbloqueio("D4", 2));

        assertThat(publicados).map(o -> ((DesbloqueioEfetuado) o).tipoUtilizador())
                .containsOnly(TipoUtilizador.ASSINANTE);
    }

    private static EventoDocaRecebido desbloqueio(String doca, long seq) {
        return new EventoDocaRecebido(doca, seq, TipoEventoDoca.DESBLOQUEIO_EFETUADO, "U1", "B" + seq, "lisboa",
                TipoUtilizador.ASSINANTE, null, Instant.parse("2026-11-01T10:00:00Z").plusSeconds(seq));
    }

    private static EventoDocaRecebido encaixe(String doca, long seq) {
        return new EventoDocaRecebido(doca, seq, TipoEventoDoca.BICICLETA_ENCAIXADA, null, null, null, null,
                "D0-" + seq, Instant.parse("2026-11-01T10:00:00Z").plusSeconds(seq));
    }
}
