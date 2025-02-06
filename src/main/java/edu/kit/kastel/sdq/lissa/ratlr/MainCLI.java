package edu.kit.kastel.sdq.lissa.ratlr;

import java.nio.file.Path;

import edu.kit.kastel.sdq.lissa.ratlr.command.EvaluateCommand;
import edu.kit.kastel.sdq.lissa.ratlr.command.MergeCommand;
import edu.kit.kastel.sdq.lissa.ratlr.command.TransitiveTraceCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {MergeCommand.class, EvaluateCommand.class, TransitiveTraceCommand.class})
public final class MainCLI {

    private MainCLI() {}

    public static void main(String[] args) {
        new CommandLine(new MainCLI()).registerConverter(Path.class, Path::of).execute(args);
    }
}
