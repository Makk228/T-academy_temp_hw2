package academy.cashbackmerge.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties({"_note", "_comment"})
record RawCampaignConfig(
        @Nullable Integer boardSize,
        @Nullable Integer movesLimit,
        @Nullable List<@Nullable RawSpawnRule> spawnRules,
        @Nullable List<@Nullable RawRewardTier> rewardTable,
        @Nullable String maxRewardCashback,
        @Nullable String maxRewardAllowedRate) {

    record RawSpawnRule(@Nullable String cashback, @Nullable String probability) {}

    record RawRewardTier(
            @Nullable String cashback, @Nullable String reward) {}
}
