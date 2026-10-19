package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.model.Cashback;
import academy.cashbackmerge.reward.RewardTable;
import academy.cashbackmerge.simulation.SimulationReport.RewardCount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SplittableRandom;
import java.util.TreeMap;

public final class Simulator {

    private Simulator() {}

    public static SimulationReport run(
            CampaignConfig config, String configSource, Strategy strategy, int players, long seed) {
        if (players <= 0) {
            throw new IllegalArgumentException("число игроков должно быть больше 0, получено " + players);
        }
        var master = new SplittableRandom(seed);
        var maxTiles = new TreeMap<Cashback, Integer>();
        long totalScore = 0;
        long maxScore = 0;
        for (int player = 0; player < players; player++) {
            var finalState = GameRunner.play(config, strategy, master.split());
            var maxTile = finalState.board().maxTile().orElseThrow();
            maxTiles.merge(maxTile, 1, Integer::sum);
            totalScore += finalState.score();
            maxScore = Math.max(maxScore, finalState.score());
        }

        var rewardTable = RewardTable.of(config);
        int maxRewardPlayers = maxTiles.entrySet().stream()
                .filter(entry -> rewardTable.isMaxReward(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
        var status = ConstraintStatus.evaluate(maxRewardPlayers, players, config.maxRewardAllowedRate());
        return new SimulationReport(
                configSource,
                config,
                strategy.name(),
                seed,
                players,
                maxTiles,
                rewardDistribution(maxTiles, rewardTable),
                totalScore,
                maxScore,
                maxRewardPlayers,
                status);
    }

    private static List<RewardCount> rewardDistribution(SortedMap<Cashback, Integer> maxTiles, RewardTable table) {
        var byTier = new LinkedHashMap<RewardTier, Integer>();
        table.tiers().forEach(tier -> byTier.put(tier, 0));
        int withoutTier = 0;
        for (var entry : maxTiles.entrySet()) {
            var tier = table.tierFor(entry.getKey());
            if (tier.isPresent()) {
                byTier.merge(tier.get(), entry.getValue(), Integer::sum);
            } else {
                withoutTier += entry.getValue();
            }
        }
        var result = new ArrayList<RewardCount>();
        if (withoutTier > 0) {
            result.add(new RewardCount(null, withoutTier));
        }
        byTier.forEach((tier, count) -> result.add(new RewardCount(tier, count)));
        return result;
    }
}
