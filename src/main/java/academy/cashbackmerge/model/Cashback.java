package academy.cashbackmerge.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Cashback(BigDecimal percent) implements Comparable<Cashback> {

    public Cashback {
        Objects.requireNonNull(percent, "percent");
        if (percent.signum() <= 0) {
            throw new IllegalArgumentException("кэшбэк должен быть больше 0, получено " + percent.toPlainString());
        }
        percent = percent.stripTrailingZeros();
    }

    public static Cashback parse(String text) {
        return new Cashback(Decimals.parse(text));
    }

    public static Cashback of(long percent) {
        return new Cashback(BigDecimal.valueOf(percent));
    }

    @Override
    public int compareTo(Cashback other) {
        return percent.compareTo(other.percent);
    }

    public String toPlainString() {
        return percent.toPlainString();
    }

    @Override
    public String toString() {
        return toPlainString() + "%";
    }
}
