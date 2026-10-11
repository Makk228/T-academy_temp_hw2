package academy.cashbackmerge.cli;

import academy.cashbackmerge.config.InvalidConfigException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(
        name = "cashback-merge",
        mixinStandardHelpOptions = true,
        version = "cashback-merge 1.0.0",
        description = "Мини-игра в духе 2048 с плитками кэшбэка и симуляцией промоакции.",
        subcommands = {NewGameCommand.class, CommandLine.HelpCommand.class})
public final class CashbackMergeCommand implements Runnable {

    public static final int EXIT_INVALID_CONFIG = 3;

    @Spec
    @SuppressWarnings("NullAway.Init")
    private CommandSpec spec;

    public static CommandLine commandLine() {
        return new CommandLine(new CashbackMergeCommand())
                .setExecutionExceptionHandler(CashbackMergeCommand::handleExecutionException);
    }

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }

    private static int handleExecutionException(Exception exception, CommandLine commandLine, ParseResult parseResult)
            throws Exception {
        if (exception instanceof InvalidConfigException invalidConfig) {
            commandLine.getErr().println(invalidConfig.getMessage());
            return EXIT_INVALID_CONFIG;
        }
        throw exception;
    }
}
