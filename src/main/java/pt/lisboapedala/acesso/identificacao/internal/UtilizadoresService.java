package pt.lisboapedala.acesso.identificacao.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import pt.lisboapedala.acesso.identificacao.api.CartaoNaveganteElegivel;
import pt.lisboapedala.acesso.identificacao.api.DadosFiscais;
import pt.lisboapedala.acesso.identificacao.api.Elegibilidade;
import pt.lisboapedala.acesso.identificacao.api.ElegibilidadeProvider;
import pt.lisboapedala.acesso.identificacao.api.MotivoInelegibilidade;
import pt.lisboapedala.comum.TipoUtilizador;
import pt.lisboapedala.comum.UtilizadorId;

/**
 * Contas e elegibilidade. Em memória, como o resto do projeto.
 * O NIF é guardado através do DadosFiscaisService já existente (usado por faturacao),
 * para haver uma só fonte de dados fiscais.
 */
@Service
public class UtilizadoresService implements ElegibilidadeProvider {

    private final Map<UtilizadorId, Utilizador> contas = new ConcurrentHashMap<>();
    private final DadosFiscaisService dadosFiscais;
    private final ValidadorNavegante navegante;

    UtilizadoresService(DadosFiscaisService dadosFiscais, ValidadorNavegante navegante) {
        this.dadosFiscais = dadosFiscais;
        this.navegante = navegante;
    }

    public synchronized Utilizador registar(String nome, String email, String nif, TipoUtilizador tipo,
                                            String cartaoNavegante) {
        if (nome == null || nome.isBlank() || email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Nome e email válidos são obrigatórios");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de utilizador obrigatório");
        }
        if (!ValidadorNif.valido(nif)) {
            throw new IllegalArgumentException("NIF inválido");
        }
        if (contas.values().stream().anyMatch(u -> u.email().equalsIgnoreCase(email))) {
            throw new IllegalArgumentException("Já existe uma conta com este email");
        }
        if (tipo == TipoUtilizador.ASSINANTE && !navegante.passeValido(cartaoNavegante)) {
            throw new IllegalArgumentException("Assinantes precisam de um cartão Navegante válido");
        }
        var id = new UtilizadorId(UUID.randomUUID().toString());
        var conta = new Utilizador(id, nome, email, tipo, EstadoConta.ATIVA, cartaoNavegante, Instant.now());
        dadosFiscais.registar(new DadosFiscais(id, nif));
        contas.put(id, conta);
        return conta;
    }

    public Optional<Utilizador> procurar(UtilizadorId id) {
        return Optional.ofNullable(contas.get(id));
    }

    public Utilizador alterarEstado(UtilizadorId id, EstadoConta estado) {
        Utilizador atualizado = contas.computeIfPresent(id, (k, u) -> u.comEstado(estado));
        if (atualizado == null) {
            throw new IllegalArgumentException("Utilizador inexistente: " + id.valor());
        }
        return atualizado;
    }

    @Override
    public Elegibilidade verificar(UtilizadorId utilizadorId) {
        Utilizador u = contas.get(utilizadorId);
        if (u == null) {
            return Elegibilidade.inelegivel(utilizadorId, MotivoInelegibilidade.CONTA_INEXISTENTE, null);
        }
        if (u.estado() != EstadoConta.ATIVA) {
            return Elegibilidade.inelegivel(utilizadorId, MotivoInelegibilidade.CONTA_SUSPENSA, u.tipo());
        }
        if (u.tipo() == TipoUtilizador.ASSINANTE && !navegante.passeValido(u.numeroCartaoNavegante())) {
            return Elegibilidade.inelegivel(utilizadorId, MotivoInelegibilidade.PASSE_NAVEGANTE_INVALIDO, u.tipo());
        }
        return Elegibilidade.elegivel(utilizadorId, u.tipo());
    }

    @Override
    public List<CartaoNaveganteElegivel> cartoesNaveganteElegiveis() {
        return contas.values().stream()
                .filter(u -> u.estado() == EstadoConta.ATIVA && u.tipo() == TipoUtilizador.ASSINANTE)
                .filter(u -> navegante.passeValido(u.numeroCartaoNavegante()))
                .map(u -> new CartaoNaveganteElegivel(u.numeroCartaoNavegante(), u.id()))
                .toList();
    }
}
