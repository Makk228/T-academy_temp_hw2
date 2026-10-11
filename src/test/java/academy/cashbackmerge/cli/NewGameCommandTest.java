package academy.cashbackmerge.cli;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Команда new-game")
class NewGameCommandTest {

    private static final String EXAMPLE_CONFIG =
            Path.of("config", "campaign.example.json").toString();

    @Test
    @DisplayName("Печатает seed и поле; одинаковый seed — одинаковый вывод")
    void printsReproducibleBoard() {
        var first = CliRunner.run("new-game", "--config", EXAMPLE_CONFIG, "--seed", "42");
        var second = CliRunner.run("new-game", "--config", EXAMPLE_CONFIG, "--seed", "42");

        assertThat(first.exitCode()).as("%s", first).isZero();
        assertThat(first.stdout())
                .startsWith("Seed: 42")
                .contains("Ходы: 0 из 100")
                .contains("+-----+");
        assertThat(second.stdout()).isEqualTo(first.stdout());
    }

    @Test
    @DisplayName("Некорректный конфиг — список ошибок в stderr и ненулевой код")
    void invalidConfigIsReported(@TempDir Path directory) throws IOException {
        var config = directory.resolve("broken.json");
        Files.writeString(config, "{ \"movesLimit\": 0 }");

        var result = CliRunner.run("new-game", "--config", config.toString());

        assertThat(result.exitCode()).isEqualTo(CashbackMergeCommand.EXIT_INVALID_CONFIG);
        assertThat(result.stderr())
                .contains("Конфиг кампании некорректен")
                .contains("spawnRules: обязательное поле не задано");
        assertThat(result.stdout()).isEmpty();
    }
}
