package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.UmlComponent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.UmlInterface;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.UmlModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.UmlModelRoot;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.OwnedOperation;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This preprocessor extracts information from a given UML model.
 * Configuration:
 * <ul>
 * <li> includeUsages: whether to include the usages of components
 * <li> includeOperations: whether to include the operations of components and interfaces
 * <li> includeInterfaceRealizations: whether to include the interface realizations of components
 * </ul>
 */
public class ModelUMLPreprocessor extends Preprocessor {
    private final boolean includeUsages;
    private final boolean includeOperations;
    private final boolean includeInterfaceRealizations;

    public ModelUMLPreprocessor(Configuration.ModuleConfiguration configuration) {
        this.includeUsages = configuration.argumentAsBoolean("includeUsages", true);
        this.includeOperations = configuration.argumentAsBoolean("includeOperations", true);
        this.includeInterfaceRealizations = configuration.argumentAsBoolean("includeInterfaceRealizations", true);
    }

    @Override
    public List<Element> preprocess(List<Artifact> artifacts) {
        List<Element> elements = new ArrayList<>();

        for (Artifact artifact : artifacts) {
            Element artifactAsElement = new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, false);
            elements.add(artifactAsElement);

            String xml = artifact.getContent();
            UmlModelRoot umlModel = new UmlModel(new ByteArrayInputStream(xml.getBytes())).getModel();

            AtomicInteger counter = new AtomicInteger(0);
            for (UmlComponent umlComponent : umlModel.getComponents()) {
                this.addComponent(counter, umlComponent, artifactAsElement, elements);
            }

            for (UmlInterface umlInterface : umlModel.getInterfaces()) {
                this.addInterface(counter, umlInterface, artifactAsElement, elements);
            }
        }
        return elements;
    }

    private void addInterface(AtomicInteger counter, UmlInterface umlInterface, Element artifactAsElement, List<Element> elements) {
        Set<String> representation = new LinkedHashSet<>();
        representation.add("Type: " + umlInterface.getType() + ", Name: " + umlInterface.getName());
        if (includeOperations) {
            for (OwnedOperation operation : umlInterface.getOperations()) {
                representation.add("\nOperation: " + operation.getName());
            }
        }
        String content = String.join("", representation);
        String identifier = artifactAsElement.getIdentifier() + SEPARATOR + counter.getAndIncrement() + SEPARATOR + umlInterface.getId();
        Element resultingElement = new Element(identifier, artifactAsElement.getType(), content, 1, artifactAsElement, false);
        elements.add(resultingElement);
    }

    private void addComponent(AtomicInteger counter, UmlComponent component, Element artifactAsElement, List<Element> elements) {
        Set<String> representation = new LinkedHashSet<>();
        representation.add("Type: " + component.getType() + ", Name: " + component.getName());
        if (includeInterfaceRealizations) {
            for (UmlInterface umlInterface : component.getProvided()) {
                representation.add("\nInterface Realization: " + umlInterface.getName());
            }
        }
        if (includeOperations) {
            for (OwnedOperation operation : component.getProvided().stream().flatMap(it -> it.getOperations().stream()).toList()) {
                representation.add("\nOperation: " + operation.getName());
            }
        }
        if (includeUsages) {
            for (UmlInterface usedComponent : component.getRequired()) {
                representation.add("\nUses: " + usedComponent.getName());
            }
        }

        String content = String.join("", representation);
        String identifier = artifactAsElement.getIdentifier() + SEPARATOR + counter.getAndIncrement() + SEPARATOR + component.getId();
        Element resultingElement = new Element(identifier, artifactAsElement.getType(), content, 1, artifactAsElement, true);
        elements.add(resultingElement);
    }
}
