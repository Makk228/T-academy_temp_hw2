package academy.cashbackmerge.config;

import academy.cashbackmerge.model.Cashback;
import java.math.BigDecimal;
import java.util.Objects;

public record RewardTier(Cashback cashback, BigDecimal reward) {

    public RewardTier {
        Objects.requireNonNull(cashback, "cashback");
        Objects.requireNonNull(reward, "reward");
        reward = reward.stripTrailingZeros();
    }
}
