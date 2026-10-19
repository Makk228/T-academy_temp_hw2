package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.engine.GameFactory;
import academy.cashbackmerge.engine.RandomTileSpawner;
import academy.cashbackmerge.report.JsonReport;
import academy.cashbackmerge.report.MarkdownReport;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.support.CampaignJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    @Test
    @DisplayName("Одинаковый конфиг и seed дают одинаковое начальное поле")
    void sameConfigAndSeedProduceSameInitialBoard() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var first = GameFactory.newGame(config, RandomTileSpawner.seeded(config.spawnRules(), 42));
        var second = GameFactory.newGame(config, RandomTileSpawner.seeded(config.spawnRules(), 42));

        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var first = Simulator.run(config, "test.json", new RandomStrategy(), 200, 42);
        var second = Simulator.run(config, "test.json", new RandomStrategy(), 200, 42);
        var otherSeed = Simulator.run(config, "test.json", new RandomStrategy(), 200, 43);

        assertThat(second).isEqualTo(first);
        assertThat(MarkdownReport.render(second)).isEqualTo(MarkdownReport.render(first));
        assertThat(JsonReport.render(second)).isEqualTo(JsonReport.render(first));
        assertThat(otherSeed.maxTiles()).as("другой seed — другие партии").isNotEqualTo(first.maxTiles());
    }
}
