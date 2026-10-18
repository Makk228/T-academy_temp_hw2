package academy.cashbackmerge.config;

import academy.cashbackmerge.util.Decimals;
import java.math.BigDecimal;
import java.util.Objects;

public record Probability(BigDecimal value) {

    public Probability {
        Objects.requireNonNull(value, "value");
        if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("ожидалось число от 0 до 1, получено " + value.toPlainString());
        }
        value = value.stripTrailingZeros();
    }

    public static Probability parse(String text) {
        return new Probability(Decimals.parse(text));
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
