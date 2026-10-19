package academy.cashbackmerge.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.engine.Direction;
import academy.cashbackmerge.engine.MoveEngine;
import academy.cashbackmerge.model.GameState;
import academy.cashbackmerge.support.CampaignJson;
import academy.cashbackmerge.support.TestBoards;
import java.math.BigDecimal;
import java.util.SplittableRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Симуляция")
class SimulatorTest {

    @Test
    @DisplayName("Стратегия random перебирает все четыре хода в случайном порядке")
    void randomStrategyReturnsPermutation() {
        var strategy = new RandomStrategy();
        var state = GameState.initial(TestBoards.of("1,.,.,."), 10);
        var random = new SplittableRandom(1);

        var first = strategy.preferences(state, random);
        var orders = IntStream.range(0, 50)
                .mapToObj(i -> strategy.preferences(state, random))
                .distinct()
                .count();

        assertThat(first).containsExactlyInAnyOrder(Direction.values());
        assertThat(orders).as("порядок меняется от хода к ходу").isGreaterThan(1);
    }

    @Test
    @DisplayName("Партия идёт до лимита ходов или до заблокированного поля")
    void gameRunsUntilLimitOrBlocked() {
        var config = CampaignConfigLoader.parse(CampaignJson.with("15", CampaignJson.DEFAULT_SPAWN_RULES));

        for (long seed = 0; seed < 20; seed++) {
            var state = GameRunner.play(config, new RandomStrategy(), new SplittableRandom(seed));

            assertThat(MoveEngine.isGameOver(state)).isTrue();
            assertThat(state.movesUsed()).isLessThanOrEqualTo(15);
        }
    }

    @Test
    @DisplayName("Счётчики отчёта согласованы: игроки, награды, score")
    void reportCountersAreConsistent() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        var report = Simulator.run(config, "test.json", new RandomStrategy(), 150, 3);

        assertThat(report.players()).isEqualTo(150);
        assertThat(report.rewards())
                .as("в отчёте все строки таблицы наград, даже без игроков")
                .hasSizeGreaterThanOrEqualTo(config.rewardTable().size());
        assertThat(report.averageScore()).isLessThanOrEqualTo(BigDecimal.valueOf(report.maxScore()));
        assertThat(report.maxRewardRateMargin95().signum()).isNotNegative();
    }

    @Test
    @DisplayName("Ноль игроков и неизвестная стратегия отклоняются")
    void invalidArgumentsAreRejected() {
        var config = CampaignConfigLoader.parse(CampaignJson.valid());

        assertThatThrownBy(() -> Simulator.run(config, "test.json", new RandomStrategy(), 0, 1))
                .hasMessageContaining("больше 0");
        assertThatThrownBy(() -> Strategies.byName("smart"))
                .hasMessage("неизвестная стратегия 'smart', доступны: random");
        assertThat(Strategies.byName("random")).isInstanceOf(RandomStrategy.class);
    }
}
