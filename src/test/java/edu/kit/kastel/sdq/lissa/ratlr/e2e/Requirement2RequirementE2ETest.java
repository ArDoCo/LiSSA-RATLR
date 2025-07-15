/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.e2e;

import static edu.kit.kastel.sdq.lissa.ratlr.Statistics.getTraceLinksFromGoldStandard;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Environment;

class Requirement2RequirementE2ETest {

    @Test
    void testEnd2End() throws Exception {

        try (MockedStatic<Environment> mockedEnvironment = Mockito.mockStatic(Environment.class)) {
            setupEnvironment(mockedEnvironment);

            File config = new File("src/test/resources/warc/config.json");
            Assertions.assertTrue(config.exists());

            Evaluation evaluation = new Evaluation(config.toPath());
            var traceLinks = evaluation.run();

            Set<TraceLink> validTraceLinks =
                    getTraceLinksFromGoldStandard(evaluation.getConfiguration().goldStandardConfiguration());

            ClassificationMetricsCalculator cmc = ClassificationMetricsCalculator.getInstance();
            var classification = cmc.calculateMetrics(traceLinks, validTraceLinks, null);
            classification.prettyPrint();
            Assertions.assertEquals(0.38, classification.getPrecision(), 1E-8);
            Assertions.assertEquals(0.6985294117647058, classification.getRecall(), 1E-8);
            Assertions.assertEquals(0.49222797927461137, classification.getF1(), 1E-8);
        }
    }

    private void setupEnvironment(MockedStatic<Environment> mockedEnvironment) {
        mockedEnvironment.when(() -> Environment.getenv(Mockito.any())).thenReturn("DUMMY_ENV_VAR");
        mockedEnvironment.when(() -> Environment.getenvNonNull(Mockito.any())).thenReturn("DUMMY_ENV_VAR");
    }
}
