/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.cli.command;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import picocli.CommandLine;

@CommandLine.Command(
        name = "eval",
        mixinStandardHelpOptions = true,
        description = "Invokes the pipeline and evaluates it")
public class EvaluateCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EvaluateCommand.class);

    @CommandLine.Option(
            names = {"-c", "--configs"},
            arity = "1..*",
            description =
                    "Specifies one or more config paths to be invoked by the pipeline iteratively. If the path points to a directory, all files inside are chosen to get invoked.")
    private Path[] configs;

    @Override
    public void run() {
        List<Path> configsToEvaluate = loadConfigs();
        logger.info("Found {} config files to invoke", configsToEvaluate.size());

        for (Path config : configsToEvaluate) {
            logger.info("Invoking the pipeline with '{}'", config);
            try {
                var evaluation = new Evaluation(config);
                evaluation.run();
            } catch (Exception e) {
                logger.warn("Configuration '{}' threw an exception: {}", config, e.getMessage());
            }
        }
    }

    private List<Path> loadConfigs() {
        List<Path> configsToEvaluate = new LinkedList<>();
        if (configs == null) {
            Path defaultConfig = Path.of("config.json");
            if (Files.notExists(defaultConfig)) {
                logger.warn(
                        "Default config '{}' does not exist and no config paths provided, so there is nothing to work with",
                        defaultConfig);
                return List.of();
            }
            configsToEvaluate.add(defaultConfig);
        } else {
            addSpecifiedConfigPaths(configsToEvaluate);
        }

        return configsToEvaluate;
    }

    private void addSpecifiedConfigPaths(List<Path> configsToEvaluate) {
        for (Path configPath : configs) {
            if (Files.notExists(configPath)) {
                logger.warn("Specified config path '{}' does not exist", configPath);
                continue;
            }

            if (!Files.isDirectory(configPath)) {
                configsToEvaluate.add(configPath);
                continue;
            }

            try (DirectoryStream<Path> configDir = Files.newDirectoryStream(configPath)) {
                for (Path configDirEntry : configDir) {
                    if (!Files.isDirectory(configDirEntry)) {
                        configsToEvaluate.add(configDirEntry);
                    }
                }
            } catch (IOException e) {
                logger.warn(
                        "Skipping specified config path '{}' due to causing an exception: {}",
                        configPath,
                        e.getMessage());
            }
        }
    }
}
