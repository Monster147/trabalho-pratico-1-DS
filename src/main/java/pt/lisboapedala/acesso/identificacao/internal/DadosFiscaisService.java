package pt.lisboapedala.acesso.identificacao.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import pt.lisboapedala.acesso.identificacao.api.DadosFiscais;
import pt.lisboapedala.acesso.identificacao.api.DadosFiscaisProvider;
import pt.lisboapedala.comum.UtilizadorId;

@Service
public class DadosFiscaisService implements DadosFiscaisProvider {
    private final Map<UtilizadorId, DadosFiscais> dados = new ConcurrentHashMap<>();

    public void registar(DadosFiscais fiscais) {
        dados.put(fiscais.utilizadorId(), fiscais);
    }

    @Override
    public DadosFiscais obterDadosFiscais(UtilizadorId utilizadorId) {
        DadosFiscais fiscais = dados.get(utilizadorId);
        if (fiscais == null) throw new IllegalStateException("Dados fiscais inexistentes para " + utilizadorId.valor());
        return fiscais;
    }
}
