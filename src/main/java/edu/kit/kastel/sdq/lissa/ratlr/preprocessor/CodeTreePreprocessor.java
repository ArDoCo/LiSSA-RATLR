/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import java.util.*;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * This preprocessor creates a tree structure from the code artifacts. It does not only consider code files but also the package declaration.
 * Configuration:
 * <ul>
 * <li> language: the language of the code
 * </ul>
 */
public class CodeTreePreprocessor extends Preprocessor {

    private final Language language;
    private final boolean compareClasses;

    public CodeTreePreprocessor(ModuleConfiguration configuration) {
        this.language = Objects.requireNonNull(Language.valueOf(configuration.argumentAsString("language")));
        this.compareClasses = configuration.argumentAsBoolean("compare_classes", false);
    }

    @Override
    public List<Element> preprocess(List<Artifact> artifacts) {
        return switch (language) {
            case JAVA -> createJavaTree(artifacts);
        };
    }

    private List<Element> createJavaTree(List<Artifact> artifacts) {
        List<Element> result = new ArrayList<>();

        Map<String, List<Artifact>> packagesToClasses = new HashMap<>();
        for (Artifact artifact : artifacts) {
            List<String> packageDeclaration = Arrays.stream(
                            artifact.getContent().split("\n"))
                    .filter(line -> line.trim().startsWith("package"))
                    .toList();
            assert packageDeclaration.size() <= 1;
            String packageName = packageDeclaration.isEmpty()
                    ? ""
                    : packageDeclaration.getFirst().split(" ")[1].replace(";", "");
            packagesToClasses.putIfAbsent(packageName, new ArrayList<>());
            packagesToClasses.get(packageName).add(artifact);
        }

        for (Map.Entry<String, List<Artifact>> entry : packagesToClasses.entrySet()) {
            String packageName = entry.getKey();
            List<Artifact> classes = entry.getValue();
            String packageDescription =
                    """
                    This package is called %s and contains the following classes: %s
                    """
                            .formatted(
                                    packageName,
                                    classes.stream()
                                            .map(Artifact::getIdentifier)
                                            .toList());

            Element packageElement = new Element(
                    "package-" + packageName, "source code package definition", packageDescription, 0, null, true);
            result.add(packageElement);
            for (Artifact clazz : classes) {
                Element classElement = new Element(
                        clazz.getIdentifier(),
                        "source code class definition",
                        clazz.getContent(),
                        1,
                        packageElement,
                        compareClasses);
                result.add(classElement);
            }
        }
        return result;
    }

    public enum Language {
        JAVA
    }
}
