package edu.kit.kastel.filesorter.view.command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Metrics available when displaying the similarity of a comparison in the editing mode.
 */
enum ComparisonMetric {

    /** Symmetric similarity {@code 2m / (a + b)}. */
    SYMMETRIC("AVG", "AVERAGE", "SYMMETRIC", "SYMMETRICAL"),

    /** Similarity with respect to the first text {@code m / a}. */
    FIRST("FIRST", "LEFT"),

    /** Similarity with respect to the second text {@code m / b}. */
    SECOND("SECOND", "RIGHT"),

    /** Maximum similarity between both texts. */
    MAXIMUM("MAX", "MAXIMUM"),

    /** Minimum similarity between both texts. */
    MINIMUM("MIN", "MINIMUM");

    private final Set<String> aliases;

    ComparisonMetric(String... aliases) {
        this.aliases = new HashSet<>();
        this.aliases.add(name());
        Arrays.stream(aliases)
                .map(alias -> alias.toUpperCase(Locale.ROOT))
                .forEach(this.aliases::add);
    }

    double compute(int totalLength, int firstTokenCount, int secondTokenCount) {
        double similarityToFirst = ratio(totalLength, firstTokenCount);
        double similarityToSecond = ratio(totalLength, secondTokenCount);
        return switch (this) {
            case SYMMETRIC -> {
                int combined = firstTokenCount + secondTokenCount;
                yield combined == 0 ? 0 : (2.0 * totalLength) / combined;
            }
            case FIRST -> similarityToFirst;
            case SECOND -> similarityToSecond;
            case MAXIMUM -> Math.max(similarityToFirst, similarityToSecond);
            case MINIMUM -> Math.min(similarityToFirst, similarityToSecond);
        };
    }

    private static double ratio(int length, int tokenCount) {
        if (tokenCount == 0 || length == 0) {
            return 0;
        }
        return (double) length / tokenCount;
    }

    static ComparisonMetric fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (ComparisonMetric metric : values()) {
            if (metric.aliases.contains(normalized)) {
                return metric;
            }
        }
        return null;
    }
}

