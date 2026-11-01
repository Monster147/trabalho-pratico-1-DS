package pt.lisboapedala.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApiPublicaTest {
    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter().importPackages("pt.lisboapedala");
    }

    @Test
    void classesInternasNaoSaoAcedidasPorOutrosPacotes() {
        noClasses().that().resideOutsideOfPackages("..internal..", "..comum..")
                .should().dependOnClassesThat().resideInAnyPackage("..internal..")
                .check(classes);
    }
}
