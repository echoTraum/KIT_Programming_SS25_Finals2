package edu.kit.kastel.filesorter.view.command;

import java.util.Locale;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Enumeration of metrics that can be used to compare program pairs when listing results.
 */
public enum ListMetric {

    /** Symmetric similarity: {@code 2m / (a + b)}. */
    AVG(PairSummary::symmetricSimilarity, MetricValueType.PERCENTAGE),

    /** Maximum similarity: {@code max(m / a, m / b)}. */
    MAX(PairSummary::maximumSimilarity, MetricValueType.PERCENTAGE),

    /** Minimum similarity: {@code min(m / a, m / b)}. */
    MIN(PairSummary::minimumSimilarity, MetricValueType.PERCENTAGE),

    /** Length of the longest match. */
    LONG(summary -> summary.longestMatchLength(), MetricValueType.INTEGER),

    /** Sum of the lengths of all matches. */
    LEN(summary -> summary.totalMatchLength(), MetricValueType.INTEGER);

    private final ToDoubleFunction<PairSummary> extractor;
    private final MetricValueType valueType;

    ListMetric(ToDoubleFunction<PairSummary> extractor, MetricValueType valueType) {
        this.extractor = extractor;
        this.valueType = valueType;
    }

    double extract(PairSummary summary) {
        return this.extractor.applyAsDouble(summary);
    }

    String format(double value) {
        return switch (this.valueType) {
            case INTEGER -> Long.toString(Math.round(value));
            case PERCENTAGE -> NumberFormatUtil.formatPercentage(value);
        };
    }

    /**
     * Parses the provided string into a {@link ListMetric}.
     *
     * @param value the textual representation of the metric
     * @return the parsed metric or {@code null} if the value does not correspond to a metric
     */
    public static ListMetric fromString(String value) {
        Objects.requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "AVG", "AVERAGE" -> AVG;
            case "MAX", "MAXIMUM" -> MAX;
            case "MIN", "MINIMUM" -> MIN;
            case "LONG", "LONGEST" -> LONG;
            case "LEN", "LENGTH" -> LEN;
            default -> null;
        };
    }

    private enum MetricValueType {
        INTEGER,
        PERCENTAGE
    }
}
