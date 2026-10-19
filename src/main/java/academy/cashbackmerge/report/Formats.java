package academy.cashbackmerge.report;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Formats {

    private static final int PERCENT_SCALE = 2;

    private Formats() {}

    public static String percent(BigDecimal fraction) {
        return fraction.movePointRight(2)
                        .setScale(PERCENT_SCALE, RoundingMode.HALF_UP)
                        .toPlainString() + "%";
    }
}
