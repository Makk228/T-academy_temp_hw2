package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.engine.GameFactory;
import academy.cashbackmerge.engine.RandomTileSpawner;
import academy.cashbackmerge.support.CampaignJson;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        fail("Тест не реализован");
    }
}
