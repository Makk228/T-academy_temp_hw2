package academy.cashbackmerge.model;

import academy.cashbackmerge.util.Decimals;

public record Cashback(int percent) implements Comparable<Cashback> {

    public Cashback {
        if (percent <= 0 || Integer.bitCount(percent) != 1) {
            throw new IllegalArgumentException(
                    "кэшбэк должен быть степенью двойки (1, 2, 4, 8, ...), получено " + percent);
        }
    }

    public static Cashback parse(String text) {
        var value = Decimals.parse(text);
        try {
            return new Cashback(value.intValueExact());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("кэшбэк должен быть целым числом процентов, получено '" + text + "'", e);
        }
    }

    public static Cashback of(int percent) {
        return new Cashback(percent);
    }

    @Override
    public int compareTo(Cashback other) {
        return Integer.compare(percent, other.percent);
    }

    public String toPlainString() {
        return String.valueOf(percent);
    }

    @Override
    public String toString() {
        return toPlainString() + "%";
    }
}
