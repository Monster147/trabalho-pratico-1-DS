package pt.lisboapedala.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegrasAllowedToUseTest {
    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter().importPackages("pt.lisboapedala");
    }

    @Test
    void viagensSoUsaDocasApiEComum() {
        regra("..viagem.viagens..", "..estacao.docas.internal..", "..cobranca..", "..acesso..").check(classes);
    }

    @Test
    void tarifarioSoUsaComum() {
        regra("..cobranca.tarifario..", "..estacao..", "..viagem..", "..acesso..", "..cobranca.faturacao..", "..cobranca.pagamentos..").check(classes);
    }

    @Test
    void faturacaoSoUsaApisPermitidas() {
        regra("..cobranca.faturacao..", "..estacao..", "..cobranca.pagamentos..").check(classes);
    }

    @Test
    void pagamentosSoUsaFaturacaoApiEComum() {
        regra("..cobranca.pagamentos..", "..viagem..", "..cobranca.tarifario..", "..acesso..", "..estacao..").check(classes);
    }

    @Test
    void historicoSoUsaApisPermitidas() {
        regra("..viagem.historico..", "..estacao..", "..cobranca.tarifario..").check(classes);
    }

    @Test
    void comumNaoImportaModulos() {
        noClasses().that().resideInAnyPackage("..comum..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..acesso..", "..estacao..", "..viagem..", "..cobranca..")
                .check(classes);
    }

    private static ArchRule regra(String origem, String... proibidos) {
        return noClasses().that().resideInAnyPackage(origem)
                .should().dependOnClassesThat().resideInAnyPackage(proibidos);
    }
}
