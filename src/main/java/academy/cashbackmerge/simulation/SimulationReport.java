package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.model.Cashback;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

public record SimulationReport(
        String configSource,
        CampaignConfig config,
        String strategy,
        long seed,
        int players,
        SortedMap<Cashback, Integer> maxTiles,
        List<RewardCount> rewards,
        long totalScore,
        long maxScore,
        int maxRewardPlayers,
        ConstraintStatus status) {

    public static final int RATE_SCALE = 6;

    private static final double Z_95 = 1.96;

    public record RewardCount(@Nullable RewardTier tier, int players) {

        public BigDecimal reward() {
            return tier == null ? BigDecimal.ZERO : tier.reward();
        }
    }

    public SimulationReport {
        Objects.requireNonNull(configSource, "configSource");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(status, "status");
        maxTiles = Collections.unmodifiableSortedMap(new TreeMap<>(maxTiles));
        rewards = List.copyOf(rewards);
    }

    public BigDecimal averageScore() {
        return BigDecimal.valueOf(totalScore).divide(BigDecimal.valueOf(players), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal maxRewardRate() {
        return rate(maxRewardPlayers);
    }

    public BigDecimal rate(int count) {
        return BigDecimal.valueOf(count).divide(BigDecimal.valueOf(players), RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal maxRewardRateMargin95() {
        double p = (double) maxRewardPlayers / players;
        double margin = Z_95 * Math.sqrt(p * (1 - p) / players);
        return BigDecimal.valueOf(margin).setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }
}
