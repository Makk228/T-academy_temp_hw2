package academy.cashbackmerge.config;

import academy.cashbackmerge.model.Cashback;
import java.util.Objects;

public record SpawnRule(Cashback cashback, Probability probability) {

    public SpawnRule {
        Objects.requireNonNull(cashback, "cashback");
        Objects.requireNonNull(probability, "probability");
    }
}
