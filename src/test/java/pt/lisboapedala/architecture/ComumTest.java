package pt.lisboapedala.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ComumTest {
    private static final String[] PACOTES_DE_APLICACAO = {
            "pt.lisboapedala.acesso..",
            "pt.lisboapedala.estacao..",
            "pt.lisboapedala.viagem..",
            "pt.lisboapedala.cobranca..",
            "pt.lisboapedala.frota..",
            "pt.lisboapedala.municipio.."
    };

    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter().importPackages("pt.lisboapedala");
    }

    @Test
    void comumNaoDependeDeCodigoDeAplicacao() {
        noClasses().that().resideInAnyPackage("..comum..")
                .should().dependOnClassesThat().resideInAnyPackage(PACOTES_DE_APLICACAO)
                .check(classes);
    }

    @Test
    void tiposComunsNaoDependemDeFrameworks() {
        noClasses().that().resideInAnyPackage("..comum..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "com.tngtech..")
                .check(classes);
    }
}
