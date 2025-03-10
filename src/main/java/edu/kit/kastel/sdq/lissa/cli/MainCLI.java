/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.cli;

import java.nio.file.Path;

import edu.kit.kastel.sdq.lissa.cli.command.EvaluateCommand;
import edu.kit.kastel.sdq.lissa.cli.command.TransitiveTraceCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {EvaluateCommand.class, TransitiveTraceCommand.class})
public final class MainCLI {

    private MainCLI() {}

    public static void main(String[] args) {
        new CommandLine(new MainCLI()).registerConverter(Path.class, Path::of).execute(args);
    }
}
