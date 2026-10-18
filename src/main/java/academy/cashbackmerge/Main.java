package academy.cashbackmerge;

import academy.cashbackmerge.cli.CashbackMergeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа в Cashback Merge. */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) {
        LOG.debug("Аргументы запуска: {}", String.join(" ", args));

        int exitCode = CashbackMergeCommand.commandLine().execute(args);
        System.exit(exitCode);
    }
}
