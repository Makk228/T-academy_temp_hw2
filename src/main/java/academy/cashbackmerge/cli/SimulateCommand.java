package academy.cashbackmerge.cli;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.report.Formats;
import academy.cashbackmerge.report.ReportFormat;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.simulation.Strategies;
import academy.cashbackmerge.simulation.Strategy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(
        name = "simulate",
        mixinStandardHelpOptions = true,
        description = "Сыграть много партий по конфигу кампании и проверить долю максимальной награды.")
final class SimulateCommand implements Callable<Integer> {

    @Spec
    @SuppressWarnings("NullAway.Init")
    private CommandSpec spec;

    @Option(
            names = "--config",
            required = true,
            paramLabel = "FILE",
            description = "JSON-конфиг кампании, например config/campaign.example.json.")
    @SuppressWarnings("NullAway.Init")
    private Path config;

    @Option(
            names = "--players",
            paramLabel = "N",
            defaultValue = "10000",
            description = "Число партий. По умолчанию ${DEFAULT-VALUE}.")
    private int players;

    @Option(
            names = "--strategy",
            paramLabel = "NAME",
            defaultValue = "random",
            description = "Стратегия игроков: ${COMPLETION-CANDIDATES}. По умолчанию ${DEFAULT-VALUE}.",
            completionCandidates = StrategyNames.class)
    @SuppressWarnings("NullAway.Init")
    private String strategy;

    @Option(
            names = "--seed",
            paramLabel = "SEED",
            description = "Seed симуляции. Одинаковый seed — одинаковый отчёт. По умолчанию случайный.")
    private @Nullable Long seed;

    @Option(
            names = "--output",
            paramLabel = "FILE",
            description = "Куда сохранить отчёт. Без опции отчёт печатается в консоль.")
    private @Nullable Path output;

    @Option(
            names = "--format",
            paramLabel = "FORMAT",
            description = "Формат отчёта: ${COMPLETION-CANDIDATES}. По умолчанию по расширению --output: "
                    + ".json — JSON, иначе Markdown.")
    private @Nullable ReportFormat format;

    @Override
    public Integer call() {
        if (players <= 0) {
            throw new ParameterException(spec.commandLine(), "--players должно быть больше 0, получено " + players);
        }
        Strategy playerStrategy;
        try {
            playerStrategy = Strategies.byName(strategy);
        } catch (IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(), "--strategy: " + e.getMessage(), e);
        }
        var campaign = CampaignConfigLoader.load(config);
        long actualSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong();

        var configSource = config.toString().replace('\\', '/');
        var report = Simulator.run(campaign, configSource, playerStrategy, players, actualSeed);

        var out = spec.commandLine().getOut();
        var target = output;
        if (target == null) {
            out.print((format != null ? format : ReportFormat.MARKDOWN).render(report));
        } else {
            var reportFormat = format != null ? format : ReportFormat.forFile(target);
            write(target, reportFormat.render(report));
            out.printf(
                    "Статус: %s. Доля максимальной награды %s при допустимой %s. Отчёт: %s%n",
                    report.status(),
                    Formats.percent(report.maxRewardRate()),
                    Formats.percent(campaign.maxRewardAllowedRate().value()),
                    target);
        }
        out.flush();
        return 0;
    }

    private static void write(Path target, String text) {
        try {
            var parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(target, text);
        } catch (IOException e) {
            throw new UncheckedIOException("не удалось записать отчёт в " + target, e);
        }
    }

    static final class StrategyNames implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Strategies.names().iterator();
        }
    }
}
