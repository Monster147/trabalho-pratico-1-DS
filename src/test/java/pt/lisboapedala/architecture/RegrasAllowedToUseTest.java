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
    void cadaComponenteRespeitaApenasDependenciasPermitidas() {
        regra("..acesso.identificacao..", "..acesso.autorizacao..", "..estacao..", "..viagem..",
                "..cobranca..", "..frota..", "..municipio..").check(classes);
        regra("..acesso.autorizacao..", "..acesso.identificacao.internal..", "..estacao.docas.internal..",
                "..estacao..", "..viagem..", "..cobranca..", "..frota.telemetria..", "..municipio..").check(classes);
        regra("..estacao.docas..", "..acesso..", "..viagem..", "..cobranca..", "..frota..", "..municipio..").check(classes);
        regra("..estacao.ocupacao..", "..acesso..", "..viagem..", "..cobranca..",
                "..estacao.docas.internal..", "..frota..", "..municipio..").check(classes);
        regra("..estacao.disponibilidade..", "..acesso..", "..viagem..", "..cobranca..",
                "..estacao.docas..", "..estacao.redistribuicao..", "..frota.telemetria.internal..",
                "..frota.manutencao.internal..", "..municipio..").check(classes);
        regra("..estacao.redistribuicao..", "..acesso..", "..viagem..", "..cobranca..",
                "..estacao.docas..", "..estacao.disponibilidade..", "..frota..", "..municipio..").check(classes);
        regra("..viagem.viagens..", "..acesso..", "..estacao.docas.internal..", "..estacao.ocupacao..",
                "..estacao.disponibilidade..", "..estacao.redistribuicao..", "..cobranca..", "..frota..", "..municipio..").check(classes);
        regra("..viagem.historico..", "..acesso.identificacao.internal..", "..estacao..",
                "..viagem.viagens.internal..", "..cobranca.tarifario..", "..cobranca.pagamentos..",
                "..frota..", "..municipio..").check(classes);
        regra("..cobranca.tarifario..", "..acesso..", "..estacao..", "..viagem..",
                "..cobranca.faturacao..", "..cobranca.pagamentos..", "..frota..", "..municipio..").check(classes);
        regra("..cobranca.faturacao..", "..acesso.identificacao.internal..", "..estacao..",
                "..viagem.viagens.internal..", "..cobranca.tarifario.internal..", "..cobranca.pagamentos..",
                "..frota..", "..municipio..").check(classes);
        regra("..cobranca.pagamentos..", "..acesso..", "..estacao..", "..viagem..",
                "..cobranca.tarifario..", "..cobranca.faturacao.internal..", "..frota..", "..municipio..").check(classes);
        regra("..frota.telemetria..", "..acesso..", "..estacao..", "..viagem..", "..cobranca..",
                "..frota.manutencao..", "..municipio..").check(classes);
        regra("..frota.manutencao..", "..acesso..", "..estacao..", "..viagem..", "..cobranca..",
                "..frota.telemetria.internal..", "..municipio..").check(classes);
        regra("..municipio.relatorios..", "..acesso..", "..estacao.docas..", "..estacao.disponibilidade..",
                "..estacao.redistribuicao..", "..viagem.historico..", "..cobranca..", "..frota..").check(classes);
    }

    @Test
    void comumNaoImportaModulos() {
        noClasses().that().resideInAnyPackage("..comum..")
                .should().dependOnClassesThat().resideInAnyPackage("..acesso..", "..estacao..",
                        "..viagem..", "..cobranca..", "..frota..", "..municipio..")
                .check(classes);
    }

    private static ArchRule regra(String origem, String... proibidos) {
        return noClasses().that().resideInAnyPackage(origem)
                .should().dependOnClassesThat().resideInAnyPackage(proibidos);
    }
}
