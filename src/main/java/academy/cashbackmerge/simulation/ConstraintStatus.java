package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.Probability;
import java.math.BigDecimal;

public enum ConstraintStatus {
    OK,
    FAILED;

    public static ConstraintStatus evaluate(int maxRewardPlayers, int players, Probability allowed) {
        var limit = allowed.value().multiply(BigDecimal.valueOf(players));
        return BigDecimal.valueOf(maxRewardPlayers).compareTo(limit) <= 0 ? OK : FAILED;
    }
}
