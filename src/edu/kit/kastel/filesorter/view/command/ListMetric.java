package edu.kit.kastel.filesorter.view.command;

import java.util.Locale;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Enumeration of metrics that can be used to compare program pairs when listing results.
 */
public enum ListMetric {

    /** Metric based on the number of matches. */
    MATCH_COUNT(PairSummary::matchCount, true),

    /** Metric based on the sum of all match lengths. */
    TOTAL_LENGTH(PairSummary::totalMatchLength, true),

    /** Metric based on the longest detected match. */
    LONGEST_MATCH(PairSummary::longestMatchLength, true),

    /** Metric based on the average match length. */
    AVERAGE_LENGTH(PairSummary::averageMatchLength, false),

    /** Metric based on the coverage of the first text. */
    FIRST_COVERAGE(PairSummary::firstCoverage, false),

    /** Metric based on the coverage of the second text. */
    SECOND_COVERAGE(PairSummary::secondCoverage, false),

    /** Metric based on the average coverage of both texts. */
    AVERAGE_COVERAGE(PairSummary::averageCoverage, false),

    /** Metric based on the minimum coverage of both texts. */
    MINIMUM_COVERAGE(PairSummary::minimumCoverage, false),

    /** Metric based on the maximum coverage of both texts. */
    MAXIMUM_COVERAGE(PairSummary::maximumCoverage, false);

    private final ToDoubleFunction<PairSummary> extractor;
    private final boolean integerMetric;

    ListMetric(ToDoubleFunction<PairSummary> extractor, boolean integerMetric) {
        this.extractor = extractor;
        this.integerMetric = integerMetric;
    }

    double extract(PairSummary summary) {
        return this.extractor.applyAsDouble(summary);
    }

    /**
     * Formats the provided value for presentation.
     *
     * @param value the value to format
     * @return the formatted value
     */
    public String format(double value) {
        if (this.integerMetric) {
            return Long.toString(Math.round(value));
        }
        return NumberFormatUtil.formatDecimal(value);
    }

    String displayName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses the provided string into a {@link ListMetric}.
     *
     * @param value the textual representation of the metric
     * @return the parsed metric or {@code null} if the value does not correspond to a metric
     */
    public static ListMetric fromString(String value) {
        Objects.requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "MATCH_COUNT", "MATCHES", "COUNT" -> MATCH_COUNT;
            case "TOTAL_LENGTH", "TOTAL", "SUM", "SUM_LENGTH" -> TOTAL_LENGTH;
            case "LONGEST_MATCH", "LONGEST", "MAX", "MAX_LENGTH" -> LONGEST_MATCH;
            case "AVERAGE_LENGTH", "AVERAGE", "AVG", "MEAN_LENGTH" -> AVERAGE_LENGTH;
            case "FIRST_COVERAGE", "COVERAGE_FIRST", "FIRST" -> FIRST_COVERAGE;
            case "SECOND_COVERAGE", "COVERAGE_SECOND", "SECOND" -> SECOND_COVERAGE;
            case "AVERAGE_COVERAGE", "COVERAGE_AVERAGE", "MEAN_COVERAGE", "OVERLAP" -> AVERAGE_COVERAGE;
            case "MINIMUM_COVERAGE", "MIN_COVERAGE", "COVERAGE_MIN" -> MINIMUM_COVERAGE;
            case "MAXIMUM_COVERAGE", "MAX_COVERAGE", "COVERAGE_MAX" -> MAXIMUM_COVERAGE;
            default -> null;
        };
    }
}
