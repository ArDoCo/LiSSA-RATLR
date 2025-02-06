/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Environment;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;

@AnalyzeClasses(packages = "edu.kit.kastel.sdq.lissa")
class ArchitectureTest {

    @ArchTest
    static final ArchRule no_getenv = noClasses()
            .that()
            .haveNameNotMatching(Environment.class.getName())
            .should()
            .callMethod(System.class, "getenv")
            .orShould()
            .callMethod(System.class, "getenv", String.class);

    @ArchTest
    static final ArchRule no_uuid_outside_key_generator = noClasses()
            .that()
            .haveNameNotMatching(KeyGenerator.class.getName())
            .should()
            .accessClassesThat()
            .haveNameMatching(UUID.class.getName());

    @ArchTest
    static final ArchRule noForEachInCollectionsOrStream = noClasses()
            .should()
            .callMethod(Stream.class, "forEach", Consumer.class)
            .orShould()
            .callMethod(Stream.class, "forEachOrdered", Consumer.class)
            .orShould()
            .callMethod(List.class, "forEach", Consumer.class)
            .orShould()
            .callMethod(List.class, "forEachOrdered", Consumer.class)
            .because("Lambdas should be functional. ForEach is typically used for side-effects.");
}
