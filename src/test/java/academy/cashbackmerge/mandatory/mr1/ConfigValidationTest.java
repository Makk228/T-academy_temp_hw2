package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.config.InvalidConfigException;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.support.CampaignJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Обязательные тесты: конфиг кампании. */
@DisplayName("MR1. Конфиг кампании")
class ConfigValidationTest {

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        assertThat(config.boardSize()).isEqualTo(4);
        assertThat(config.movesLimit()).isEqualTo(100);
        assertThat(config.spawnRules()).hasSize(2);
        assertThat(config.spawnRules().getFirst().cashback()).isEqualTo(Cashback.of(1));
        assertThat(config.spawnRules().getFirst().probability().value()).isEqualByComparingTo("0.85");
        assertThat(config.maxRewardCashback()).isEqualTo(Cashback.of(32));
        assertThat(config.maxRewardAllowedRate().value()).isEqualByComparingTo("0.2");
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected() {
        var json = CampaignJson.with("100", """
                [
                  { "cashback": "1", "probability": "0.85" },
                  { "cashback": "2", "probability": "0.10" }
                ]""");

        var error = catchThrowableOfType(InvalidConfigException.class, () -> CampaignConfigLoader.parse(json));

        assertThat(error.errors()).containsExactly("spawnRules: сумма вероятностей должна быть ровно 1, получено 0.95");
    }

    @ParameterizedTest(name = "movesLimit = {0}")
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected(String movesLimit) {
        var json = CampaignJson.with(movesLimit, CampaignJson.DEFAULT_SPAWN_RULES);

        assertThatThrownBy(() -> CampaignConfigLoader.parse(json))
                .isInstanceOf(InvalidConfigException.class)
                .hasMessageContaining("movesLimit: должен быть больше 0, получено " + movesLimit);
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected() {
        var json = CampaignJson.with("100", "[]");

        assertThatThrownBy(() -> CampaignConfigLoader.parse(json))
                .isInstanceOf(InvalidConfigException.class)
                .hasMessageContaining("spawnRules: нужно хотя бы одно правило появления плитки");
    }
}
