package academy.cashbackmerge.reward;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.model.Cashback;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RewardTable {

    private final NavigableMap<Cashback, RewardTier> tiers;
    private final Cashback maxRewardCashback;

    public RewardTable(List<RewardTier> tiers, Cashback maxRewardCashback) {
        this.tiers = tiers.stream()
                .collect(Collectors.toMap(
                        RewardTier::cashback, Function.identity(), (first, second) -> first, TreeMap::new));
        this.maxRewardCashback = Objects.requireNonNull(maxRewardCashback, "maxRewardCashback");
    }

    public static RewardTable of(CampaignConfig config) {
        return new RewardTable(config.rewardTable(), config.maxRewardCashback());
    }

    public Optional<RewardTier> tierFor(Cashback maxTile) {
        return Optional.ofNullable(tiers.floorEntry(maxTile)).map(Map.Entry::getValue);
    }

    public BigDecimal rewardFor(Cashback maxTile) {
        return tierFor(maxTile).map(RewardTier::reward).orElse(BigDecimal.ZERO);
    }

    public boolean isMaxReward(Cashback maxTile) {
        return maxTile.compareTo(maxRewardCashback) >= 0;
    }

    public List<RewardTier> tiers() {
        return List.copyOf(tiers.values());
    }

    public Cashback maxRewardCashback() {
        return maxRewardCashback;
    }
}
