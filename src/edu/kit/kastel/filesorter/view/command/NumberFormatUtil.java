package edu.kit.kastel.filesorter.view.command;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting metric values in command outputs.
 */
final class NumberFormatUtil {

    private static final int PERCENT_SCALE = 2;

    private NumberFormatUtil() {
        // utility class
    }

    static String formatPercentage(double ratio) {
        BigDecimal percentValue = BigDecimal.valueOf(ratio)
                .multiply(BigDecimal.valueOf(100))
                .setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
        return percentValue.toPlainString() + "%";
    }
}
