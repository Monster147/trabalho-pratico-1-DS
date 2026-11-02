package pt.lisboapedala.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApiPublicaTest {
    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("pt.lisboapedala");
    }

    @Test
    void classesInternasNaoSaoAcedidasPorOutrosPacotes() {
        noClasses().that().resideOutsideOfPackages("..internal..", "..comum..")
                .should().dependOnClassesThat().resideInAnyPackage("..internal..")
                .check(classes);
    }
}
