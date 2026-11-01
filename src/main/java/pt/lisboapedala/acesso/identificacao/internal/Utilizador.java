package pt.lisboapedala.acesso.identificacao.internal;

import java.time.Instant;

import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/** Conta de utilizador (privada ao componente). O NIF fica em DadosFiscaisService. */
public record Utilizador(UtilizadorId id, String nome, String email, TipoUtilizador tipo,
                         EstadoConta estado, String numeroCartaoNavegante, Instant criadoEm) {

    Utilizador comEstado(EstadoConta novo) {
        return new Utilizador(id, nome, email, tipo, novo, numeroCartaoNavegante, criadoEm);
    }
}
