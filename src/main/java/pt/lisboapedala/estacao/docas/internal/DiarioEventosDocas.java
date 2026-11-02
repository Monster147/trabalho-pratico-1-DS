package pt.lisboapedala.estacao.docas.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import pt.lisboapedala.comum.TipoUtilizador;

/**
 * Diário append-only em ficheiro: a persistência da chave de idempotência (ADR-003).
 * Cada escrita faz fsync ({@code force(true)}) antes de retornar, por isso, quando o ACK sai,
 * a chave (docaId, numeroSequencia) já sobrevive a uma queda do processo.
 *
 * <p>O resto do projeto guarda o estado em memória; este componente precisa de persistência
 * real para cumprir o ADR-003. Um ficheiro evita introduzir uma base de dados no projeto.
 * Se o grupo adotar uma BD, basta trocar esta classe por um repositório com a mesma interface.
 *
 * <p>Linhas: {@code R|<evento>} (recebido) e {@code P|<docaId>|<seq>} (publicado até seq).
 */
@Component
class DiarioEventosDocas {

    private final Map<ChaveIdempotencia, EventoDocaRecebido> recebidos = new ConcurrentHashMap<>();
    private final Map<String, Long> ultimaPublicada = new ConcurrentHashMap<>();
    private final FileChannel canal;

    DiarioEventosDocas(@Value("${lisboapedala.docas.diario:dados/docas-eventos.log}") Path ficheiro) {
        try {
            if (ficheiro.getParent() != null) {
                Files.createDirectories(ficheiro.getParent());
            }
            if (Files.exists(ficheiro)) {
                Files.readAllLines(ficheiro, UTF_8).forEach(this::reconstruir);
            }
            this.canal = FileChannel.open(ficheiro, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível abrir o diário de eventos das docas", e);
        }
    }

    boolean contem(ChaveIdempotencia chave) {
        return recebidos.containsKey(chave);
    }

    Optional<EventoDocaRecebido> obter(ChaveIdempotencia chave) {
        return Optional.ofNullable(recebidos.get(chave));
    }

    Optional<Long> ultimaPublicada(String docaId) {
        return Optional.ofNullable(ultimaPublicada.get(docaId));
    }

    Set<String> docasConhecidas() {
        return Set.copyOf(ultimaPublicada.keySet());
    }

    synchronized void registarRececao(EventoDocaRecebido e) {
        escrever(String.join("|", "R", cod(e.docaId()), Long.toString(e.numeroSequencia()), e.tipo().name(),
                cod(e.utilizadorId()), cod(e.bicicletaId()), cod(e.municipioId()), cod(e.viagemId()),
                e.ocorridoEm().toString(), e.tipoUtilizador() == null ? "" : e.tipoUtilizador().name()));
        recebidos.put(e.chave(), e);
    }

    synchronized void registarPublicacao(String docaId, long numeroSequencia) {
        escrever(String.join("|", "P", cod(docaId), Long.toString(numeroSequencia)));
        ultimaPublicada.merge(docaId, numeroSequencia, Math::max);
    }

    @PreDestroy
    void fechar() throws IOException {
        canal.close();
    }

    private void escrever(String linha) {
        try {
            canal.write(ByteBuffer.wrap((linha + "\n").getBytes(UTF_8)));
            canal.force(true);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha a persistir no diário das docas", e);
        }
    }

    private void reconstruir(String linha) {
        if (linha.isBlank()) {
            return;
        }
        List<String> c = Arrays.asList(linha.split("\\|", -1));
        if (c.get(0).equals("R") && (c.size() == 9 || c.size() == 10)) {
            // Linhas antigas (9 campos) não tinham o tipo de utilizador.
            TipoUtilizador tipo = c.size() == 10 && !c.get(9).isEmpty() ? TipoUtilizador.valueOf(c.get(9)) : null;
            var e = new EventoDocaRecebido(dec(c.get(1)), Long.parseLong(c.get(2)), TipoEventoDoca.valueOf(c.get(3)),
                    dec(c.get(4)), dec(c.get(5)), dec(c.get(6)), tipo, dec(c.get(7)), Instant.parse(c.get(8)));
            recebidos.put(e.chave(), e);
        } else if (c.get(0).equals("P") && c.size() == 3) {
            ultimaPublicada.merge(dec(c.get(1)), Long.parseLong(c.get(2)), Math::max);
        }
        // Linha incompleta (queda a meio da escrita): ignorada; sem fsync completo não houve ACK.
    }

    private static String cod(String v) {
        return v == null ? "" : URLEncoder.encode(v, UTF_8);
    }

    private static String dec(String v) {
        return v.isEmpty() ? null : URLDecoder.decode(v, UTF_8);
    }

    /** Só para testes de recuperação. */
    Map<String, Long> instantaneoPublicacoes() {
        return new HashMap<>(ultimaPublicada);
    }
}
