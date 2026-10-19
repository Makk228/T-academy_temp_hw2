package academy.cashbackmerge.cli;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.support.CliRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Команда simulate")
class SimulateCommandTest {

    private static final String EXAMPLE_CONFIG =
            Path.of("config", "campaign.example.json").toString();
    private static final String BALANCED_CONFIG =
            Path.of("config", "campaign.balanced.json").toString();

    @Test
    @DisplayName("Пишет Markdown-отчёт в файл и печатает статус")
    void writesMarkdownReport(@TempDir Path directory) throws IOException {
        var output = directory.resolve("report.md");

        var result = CliRunner.run(
                "simulate",
                "--config",
                EXAMPLE_CONFIG,
                "--players",
                "200",
                "--strategy",
                "random",
                "--seed",
                "42",
                "--output",
                output.toString());

        assertThat(result.exitCode()).as("%s", result).isZero();
        assertThat(result.stdout()).startsWith("Статус: ");
        assertThat(Files.readString(output))
                .startsWith("# Отчёт симуляции Cashback Merge")
                .contains("| Конфиг | `config/campaign.example.json` |", "| Seed | 42 |", "| Игроков | 200 |");
    }

    @Test
    @DisplayName("По расширению .json пишет JSON; сбалансированный конфиг проходит ограничение")
    void writesJsonReport(@TempDir Path directory) throws IOException {
        var output = directory.resolve("report.json");

        var result = CliRunner.run(
                "simulate",
                "--config",
                BALANCED_CONFIG,
                "--players",
                "2000",
                "--seed",
                "42",
                "--output",
                output.toString());

        assertThat(result.exitCode()).as("%s", result).isZero();
        var json = new ObjectMapper().readTree(Files.readString(output));
        assertThat(json.get("status").asText()).isEqualTo("OK");
        assertThat(json.at("/config/movesLimit").asInt()).isEqualTo(42);
    }

    @Test
    @DisplayName("Два отдельных запуска с одним seed дают одинаковый JSON — байт в байт")
    void jsonReportIsIdenticalAcrossProcesses() {
        var first = CliRunner.run(
                "simulate", "--config", EXAMPLE_CONFIG, "--players", "100", "--seed", "9", "--format", "json");
        var second = CliRunner.run(
                "simulate", "--config", EXAMPLE_CONFIG, "--players", "100", "--seed", "9", "--format", "json");

        assertThat(first.exitCode()).as("%s", first).isZero();
        assertThat(second.stdout()).isEqualTo(first.stdout());
    }

    @Test
    @DisplayName("Без --output отчёт печатается в консоль")
    void printsReportWithoutOutput() {
        var result = CliRunner.run("simulate", "--config", EXAMPLE_CONFIG, "--players", "50", "--seed", "1");

        assertThat(result.exitCode()).as("%s", result).isZero();
        assertThat(result.stdout()).contains("## Доля игроков с максимальной наградой");
    }

    @Test
    @DisplayName("Неизвестная стратегия и неположительное число игроков — ошибка аргументов")
    void invalidOptionsAreRejected() {
        var unknownStrategy =
                CliRunner.run("simulate", "--config", EXAMPLE_CONFIG, "--players", "10", "--strategy", "smart");
        var zeroPlayers = CliRunner.run("simulate", "--config", EXAMPLE_CONFIG, "--players", "0");

        assertThat(unknownStrategy.exitCode()).isEqualTo(2);
        assertThat(unknownStrategy.stderr()).contains("неизвестная стратегия 'smart', доступны: random");
        assertThat(zeroPlayers.exitCode()).isEqualTo(2);
        assertThat(zeroPlayers.stderr()).contains("--players должно быть больше 0");
    }
}
