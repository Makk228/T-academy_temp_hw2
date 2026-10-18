package academy.cashbackmerge.config;

import academy.cashbackmerge.model.Cashback;
import java.util.List;
import java.util.Objects;

public record CampaignConfig(
        int boardSize,
        int movesLimit,
        List<SpawnRule> spawnRules,
        List<RewardTier> rewardTable,
        Cashback maxRewardCashback,
        Probability maxRewardAllowedRate) {

    public CampaignConfig {
        Objects.requireNonNull(spawnRules, "spawnRules");
        Objects.requireNonNull(rewardTable, "rewardTable");
        Objects.requireNonNull(maxRewardCashback, "maxRewardCashback");
        Objects.requireNonNull(maxRewardAllowedRate, "maxRewardAllowedRate");
        spawnRules = List.copyOf(spawnRules);
        rewardTable = List.copyOf(rewardTable);

        var errors =
                CampaignConfigValidator.validate(boardSize, movesLimit, spawnRules, rewardTable, maxRewardCashback);
        if (!errors.isEmpty()) {
            throw new InvalidConfigException(errors);
        }
    }
}
