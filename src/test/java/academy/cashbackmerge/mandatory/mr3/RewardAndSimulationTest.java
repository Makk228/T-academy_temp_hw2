package academy.cashbackmerge.mandatory.mr3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.CampaignConfigLoader;
import academy.cashbackmerge.config.Probability;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.engine.SpawnDistribution;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.report.MarkdownReport;
import academy.cashbackmerge.reward.RewardTable;
import academy.cashbackmerge.simulation.ConstraintStatus;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.support.CampaignJson;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: награды, симуляция и отчёт. */
@DisplayName("MR3. Награды и симуляция")
class RewardAndSimulationTest {

    private static final int PLAYERS = 300;

    @Test
    @DisplayName("Максимальная плитка 16% даёт награду соответствующего tier")
    void rewardMatchesHighestTile() {
        var table = RewardTable.of(CampaignConfigLoader.parse(CampaignJson.valid()));

        assertThat(table.tierFor(Cashback.of(16))).contains(new RewardTier(Cashback.of(16), new BigDecimal("500")));
        assertThat(table.rewardFor(Cashback.of(16))).isEqualByComparingTo("500");
        assertThat(table.isMaxReward(Cashback.of(16))).isFalse();

        assertThat(table.rewardFor(Cashback.of(1)))
                .as("ниже младшего tier — без награды")
                .isEqualByComparingTo("0");
        assertThat(table.tierFor(Cashback.of(64)))
                .as("выше старшего tier — старший tier")
                .map(RewardTier::cashback)
                .contains(Cashback.of(32));
        assertThat(table.isMaxReward(Cashback.of(32))).isTrue();
    }

    @Test
    @DisplayName("Weighted random учитывает вероятности из конфига")
    void spawnRespectsConfiguredProbabilities() {
        var distribution = new SpawnDistribution(List.of(
                new SpawnRule(Cashback.of(1), Probability.parse("0.85")),
                new SpawnRule(Cashback.of(2), Probability.parse("0.15"))));
        var random = new SplittableRandom(42);
        int samples = 100_000;

        long ones = 0;
        for (int i = 0; i < samples; i++) {
            if (distribution.sample(random).equals(Cashback.of(1))) {
                ones++;
            }
        }

        assertThat((double) ones / samples).isCloseTo(0.85, offset(0.005));
    }

    @Test
    @DisplayName("Отчёт содержит распределение максимальных плиток и наград")
    void reportContainsDistributions() {
        var report = simulate(CampaignConfigLoader.parse(CampaignJson.valid()));

        assertThat(report.maxTiles().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum())
                .isEqualTo(PLAYERS);
        assertThat(report.rewards().stream()
                        .mapToInt(SimulationReport.RewardCount::players)
                        .sum())
                .isEqualTo(PLAYERS);

        var markdown = MarkdownReport.render(report);
        assertThat(markdown).contains("## Распределение максимальных плиток", "## Распределение наград");
        report.maxTiles().forEach((tile, count) -> assertThat(markdown).contains("| " + tile + " | " + count + " |"));
        assertThat(markdown).contains("| 32% | 2000 |", "| 16% | 500 |");
    }

    @Test
    @DisplayName("Отчёт содержит долю игроков с максимальной наградой")
    void reportContainsMaxRewardRate() {
        var report = simulate(CampaignConfigLoader.parse(CampaignJson.valid()));

        int expected = report.maxTiles().entrySet().stream()
                .filter(entry -> entry.getKey().compareTo(Cashback.of(32)) >= 0)
                .mapToInt(Map.Entry::getValue)
                .sum();
        assertThat(report.maxRewardPlayers()).isEqualTo(expected);
        assertThat(report.maxRewardRate())
                .isEqualByComparingTo(
                        BigDecimal.valueOf(expected).divide(BigDecimal.valueOf(PLAYERS), 6, RoundingMode.HALF_UP));

        assertThat(MarkdownReport.render(report))
                .contains("## Доля игроков с максимальной наградой")
                .contains("| Игроков с максимальной наградой | " + expected + " из " + PLAYERS + " |");
    }

    @Test
    @DisplayName("Статус OK, если actualMaxRewardRate <= maxRewardAllowedRate, иначе FAILED")
    void reportStatusReflectsBusinessConstraint() {
        var allowed = Probability.parse("0.20");
        assertThat(ConstraintStatus.evaluate(20, 100, allowed))
                .as("ровно на границе")
                .isEqualTo(ConstraintStatus.OK);
        assertThat(ConstraintStatus.evaluate(21, 100, allowed)).isEqualTo(ConstraintStatus.FAILED);
        assertThat(ConstraintStatus.evaluate(1, 3, Probability.parse("0.33")))
                .as("1/3 > 0.33")
                .isEqualTo(ConstraintStatus.FAILED);

        var generous = simulate(withMaxReward("2", "1"));
        assertThat(generous.status()).isEqualTo(ConstraintStatus.OK);
        assertThat(MarkdownReport.render(generous)).contains("**Статус: OK.**");

        var strict = simulate(withMaxReward("2", "0"));
        assertThat(strict.maxRewardPlayers())
                .as("плитку 2% собирает почти каждый")
                .isPositive();
        assertThat(strict.status()).isEqualTo(ConstraintStatus.FAILED);
        assertThat(MarkdownReport.render(strict)).contains("**Статус: FAILED.**");
    }

    private static SimulationReport simulate(CampaignConfig config) {
        return Simulator.run(config, "test.json", new RandomStrategy(), PLAYERS, 42);
    }

    private static CampaignConfig withMaxReward(String cashback, String allowedRate) {
        return CampaignConfigLoader.parse(CampaignJson.valid()
                .replace("\"maxRewardCashback\": \"32\"", "\"maxRewardCashback\": \"" + cashback + "\"")
                .replace("\"maxRewardAllowedRate\": \"0.20\"", "\"maxRewardAllowedRate\": \"" + allowedRate + "\""));
    }
}
