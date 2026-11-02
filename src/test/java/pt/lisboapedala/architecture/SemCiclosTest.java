package pt.lisboapedala.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SemCiclosTest {
    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter().importPackages("pt.lisboapedala");
    }

    @Test
    void componentesNaoTemCiclos() {
        SlicesRuleDefinition.slices().matching("pt.lisboapedala.(*).(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
