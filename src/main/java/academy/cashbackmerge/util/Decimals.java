package academy.cashbackmerge.util;

import java.math.BigDecimal;

public final class Decimals {

    private Decimals() {}

    public static BigDecimal parse(String text) {
        try {
            return new BigDecimal(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ожидалось число, получено '" + text + "'", e);
        }
    }
}
