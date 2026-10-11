package academy.cashbackmerge.cli;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.engine.GameFactory;
import academy.cashbackmerge.engine.RandomTileSpawner;
import academy.cashbackmerge.render.BoardRenderer;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "new-game",
        mixinStandardHelpOptions = true,
        description = "Создать начальное состояние партии по конфигу и seed и вывести поле.")
final class NewGameCommand implements Callable<Integer> {

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
            names = "--seed",
            paramLabel = "SEED",
            description = "Зерно генератора. Одинаковый seed — одинаковое поле. По умолчанию случайное.")
    private @Nullable Long seed;

    @Override
    public Integer call() {
        var campaign = CampaignConfigLoader.load(config);
        long actualSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong();
        var state = GameFactory.newGame(campaign, RandomTileSpawner.seeded(campaign.spawnRules(), actualSeed));

        var out = spec.commandLine().getOut();
        out.println("Seed: " + actualSeed);
        out.print(BoardRenderer.render(state));
        out.flush();
        return 0;
    }
}
