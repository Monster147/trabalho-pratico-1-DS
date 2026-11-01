package pt.lisboapedala.estacao.docas.internal;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import pt.lisboapedala.estacao.docas.api.PacoteAutorizacao;
import pt.lisboapedala.estacao.docas.api.SincronizacaoDocas;

/**
 * Guarda o pacote de autorização mais recente para as docas o descarregarem (modelo pull).
 * Em memória: depois de um reinício, autorizacao volta a gerá-lo no ciclo seguinte.
 */
@Service
public class SincronizacaoDocasService implements SincronizacaoDocas {

    private final AtomicReference<PacoteAutorizacao> atual = new AtomicReference<>();

    @Override
    public void disponibilizar(PacoteAutorizacao pacote) {
        atual.accumulateAndGet(pacote, (antigo, novo) ->
                antigo == null || novo.versao() > antigo.versao() ? novo : antigo);
    }

    public Optional<PacoteAutorizacao> pacoteAtual() {
        return Optional.ofNullable(atual.get());
    }
}
