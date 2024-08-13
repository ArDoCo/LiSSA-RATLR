package edu.kit.kastel.sdq.lissa.ratlr.command;

import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CommandLine.Command(name = "eval", mixinStandardHelpOptions = true, description = "Invokes the pipeline and evaluates it")
public class EvaluateCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EvaluateCommand.class);

    @CommandLine.Option(names = { "-c",
            "--configs" }, arity = "1..*", description = "Specifies one or more config paths to be invoked by the pipeline iteratively. If the path points to a directory, all files inside are chosen to get invoked.")
    private Path[] configs;

    @Override
    public void run() {
        List<Path> configsToEvaluate = new LinkedList<>();
        if (configs == null) {
            Path defaultConfig = Path.of("config.json");
            if (Files.notExists(defaultConfig)) {
                logger.warn("Default config '%s' does not exist and no config paths provided, so there is nothing to work with");
                return;
            }
            configsToEvaluate.add(defaultConfig);
        } else {
            addSpecifiedConfigPaths(configsToEvaluate);
        }

        logger.info("Found %d config files to invoke".formatted(configsToEvaluate.size()));
        configsToEvaluate.forEach(config -> {
            logger.info("Invoking the pipeline with '%s'".formatted(config));
            try {
                new Evaluation(config).run();
            } catch (Exception e) {
                logger.warn("Configuration '%s' threw an exception: %s".formatted(config, e.getMessage()));
            }
        });
    }

    private void addSpecifiedConfigPaths(List<Path> configsToEvaluate) {
        Arrays.stream(configs).forEach(configPath -> {
            if (Files.notExists(configPath)) {
                logger.warn("Specified config path '%s' does not exist".formatted(configPath));
                return;
            }

            if (Files.isDirectory(configPath)) {
                try (DirectoryStream<Path> configDir = Files.newDirectoryStream(configPath)) {
                    configDir.forEach(configDirEntry -> {
                        if (!Files.isDirectory(configDirEntry)) {
                            configsToEvaluate.add(configDirEntry);
                        }
                    });
                } catch (IOException e) {
                    logger.warn("Skipping specified config path '%s' due to causing an exception: %s".formatted(configPath, e.getMessage()));
                }
            } else {
                configsToEvaluate.add(configPath);
            }
        });
    }
}
