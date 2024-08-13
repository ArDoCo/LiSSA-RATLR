package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModelUMLPreprocessorTest {
    @Test
    void testUmlPreprocess() throws IOException {
        File model = new File("src/test/resources/mediastore.uml");
        Assertions.assertTrue(model.exists());

        ModelUMLPreprocessor preprocessor = new ModelUMLPreprocessor(new Configuration.ModuleConfiguration("dummy", Map.of()));
        var elements = preprocessor.preprocess(List.of(new Artifact("mediastore.uml", "uml", Files.readString(model.toPath()))));
        assertEquals(24, elements.size());

        var element = elements.stream().filter(it -> it.getContent().contains("Name: FileStorage")).findFirst().orElseThrow();
        assertEquals("mediastore.uml$4$_qxAiILg7EeSNPorBlo7x9g", element.getIdentifier());
        System.out.println(element.getContent());

    }
}
