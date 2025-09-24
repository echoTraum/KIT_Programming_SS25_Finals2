package edu.kit.kastel.filesorter.view.command;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting decimal numbers in command outputs.
 */
final class NumberFormatUtil {

    private static final String DECIMAL_PATTERN = "0.###";
    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    private NumberFormatUtil() {
        // utility class
    }

    /**
     * Formats the provided value using a common decimal pattern.
     *
     * @param value the value to format
     * @return the formatted value as string
     */
    static String formatDecimal(double value) {
        DecimalFormat format = new DecimalFormat(DECIMAL_PATTERN, SYMBOLS);
        return format.format(value);
    }
}
