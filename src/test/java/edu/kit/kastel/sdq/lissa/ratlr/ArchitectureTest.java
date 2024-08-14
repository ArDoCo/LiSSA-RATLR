package edu.kit.kastel.sdq.lissa.ratlr;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;

import java.util.UUID;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "edu.kit.kastel.sdq.lissa.ratlr")
class ArchitectureTest {

    @ArchTest
    static final ArchRule no_getenv = noClasses().that()
            .haveNameNotMatching(Environment.class.getName())
            .should()
            .callMethod(System.class, "getenv")
            .orShould()
            .callMethod(System.class, "getenv", String.class);

    @ArchTest
    static final ArchRule no_uuid_outside_key_generator = noClasses().that()
            .haveNameNotMatching(KeyGenerator.class.getName())
            .should()
            .accessClassesThat()
            .haveNameMatching(UUID.class.getName());
}
