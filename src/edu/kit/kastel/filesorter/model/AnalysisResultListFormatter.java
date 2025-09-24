package edu.kit.kastel.filesorter.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Formats the statistics of an {@link AnalysisResult} according to the selected metric and order.
 *
 * @author ugsrv
 */
final class AnalysisResultListFormatter {

    private static final String FORMAT_STATISTICS = "%s-%s: %s";
    private static final String DECIMAL_FORMAT_PATTERN = "%.2f";

    private AnalysisResultListFormatter() {
    }

    static String format(AnalysisResult analysisResult, ListMetric metric, SortOrder order) {
        Map<String, List<String>> tokenizedTexts = analysisResult.tokenizedTexts();
        List<String> identifiers = new ArrayList<>(tokenizedTexts.keySet());
        if (identifiers.size() < 2) {
            return "";
        }

        Map<PairKey, PairStatistics> statistics = initializeStatistics(tokenizedTexts, identifiers);
        applyMatches(analysisResult.matches(), statistics, tokenizedTexts);

        List<PairStatistics> orderedStatistics = getPairStatistics(metric, order, statistics);

        List<String> lines = new ArrayList<>(orderedStatistics.size());
        for (PairStatistics stats : orderedStatistics) {
            lines.add(formatLine(stats, metric));
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static List<PairStatistics> getPairStatistics(ListMetric metric, SortOrder order, Map<PairKey, PairStatistics> statistics) {
        List<PairStatistics> orderedStatistics = new ArrayList<>(statistics.values());
        Comparator<PairStatistics> comparator = Comparator
                .comparingDouble(stats -> computeMetricValue(stats, metric));
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(PairStatistics::firstIdentifier)
                .thenComparing(PairStatistics::secondIdentifier);
        orderedStatistics.sort(comparator);
        return orderedStatistics;
    }

    private static Map<PairKey, PairStatistics> initializeStatistics(
            Map<String, List<String>> tokenizedTexts, List<String> identifiers) {
        Map<PairKey, PairStatistics> statistics = new LinkedHashMap<>();
        for (int firstIndex = 0; firstIndex < identifiers.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < identifiers.size(); secondIndex++) {
                String firstIdentifier = identifiers.get(firstIndex);
                String secondIdentifier = identifiers.get(secondIndex);
                PairKey key = PairKey.of(firstIdentifier, secondIdentifier);
                List<String> firstTokens = tokenizedTexts.get(key.firstIdentifier());
                List<String> secondTokens = tokenizedTexts.get(key.secondIdentifier());
                statistics.put(key,
                        new PairStatistics(key.firstIdentifier(), key.secondIdentifier(),
                                firstTokens.size(), secondTokens.size()));
            }
        }
        return statistics;
    }

    private static void applyMatches(List<AnalysisMatch> matches, Map<PairKey, PairStatistics> statistics,
            Map<String, List<String>> tokenizedTexts) {
        for (AnalysisMatch match : matches) {
            PairKey key = PairKey.of(match.firstIdentifier(), match.secondIdentifier());
            PairStatistics stats = statistics.get(key);
            if (stats == null) {
                List<String> firstTokens = tokenizedTexts.get(key.firstIdentifier());
                List<String> secondTokens = tokenizedTexts.get(key.secondIdentifier());
                if (firstTokens == null || secondTokens == null) {
                    continue;
                }
                stats = new PairStatistics(key.firstIdentifier(), key.secondIdentifier(),
                        firstTokens.size(), secondTokens.size());
                statistics.put(key, stats);
            }
            stats.addMatch(match.length());
        }
    }

    private static double computeMetricValue(PairStatistics statistics, ListMetric metric) {
        return switch (metric) {
            case AVG -> statistics.averageLength();
            case MAX -> statistics.maxLength();
            case MIN -> statistics.minLength();
            case LONG -> statistics.longerTextLength();
            case LEN -> statistics.totalMatchLength();
        };
    }

    private static String formatLine(PairStatistics statistics, ListMetric metric) {
        return FORMAT_STATISTICS.formatted(statistics.firstIdentifier(),
                statistics.secondIdentifier(),
                formatMetric(statistics, metric));
    }

    private static String formatMetric(PairStatistics statistics, ListMetric metric) {
        return switch (metric) {
            case AVG -> formatDecimal(statistics.averageLength());
            case MAX -> formatInteger(statistics.maxLength());
            case MIN -> formatInteger(statistics.minLength());
            case LONG -> formatInteger(statistics.longerTextLength());
            case LEN -> formatInteger(statistics.totalMatchLength());
        };
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.ROOT, DECIMAL_FORMAT_PATTERN, value);
    }

    private static String formatInteger(long value) {
        return Long.toString(value);
    }

    private record PairKey(String firstIdentifier, String secondIdentifier) {

        private static PairKey of(String firstIdentifier, String secondIdentifier) {
            if (firstIdentifier == null || secondIdentifier == null) {
                return new PairKey(firstIdentifier, secondIdentifier);
            }
            if (firstIdentifier.compareTo(secondIdentifier) >= 0) {
                return new PairKey(firstIdentifier, secondIdentifier);
            }
            return new PairKey(secondIdentifier, firstIdentifier);
        }
    }

    private static final class PairStatistics {
        private final String firstIdentifier;
        private final String secondIdentifier;
        private final int firstTextLength;
        private final int secondTextLength;
        private long totalMatchLength;
        private int matchCount;
        private int maxMatchLength;
        private int minMatchLength = Integer.MAX_VALUE;

        PairStatistics(String firstIdentifier, String secondIdentifier, int firstTextLength, int secondTextLength) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.firstTextLength = firstTextLength;
            this.secondTextLength = secondTextLength;
        }

        private void addMatch(int length) {
            this.totalMatchLength += length;
            this.matchCount++;
            this.maxMatchLength = Math.max(this.maxMatchLength, length);
            this.minMatchLength = Math.min(this.minMatchLength, length);
        }

        private String firstIdentifier() {
            return this.firstIdentifier;
        }

        private String secondIdentifier() {
            return this.secondIdentifier;
        }

        private double averageLength() {
            if (this.matchCount == 0) {
                return 0.0d;
            }
            return (double) this.totalMatchLength / this.matchCount;
        }

        private int maxLength() {
            return this.matchCount == 0 ? 0 : this.maxMatchLength;
        }

        private int minLength() {
            return this.matchCount == 0 ? 0 : this.minMatchLength;
        }

        private long totalMatchLength() {
            return this.totalMatchLength;
        }

        private int longerTextLength() {
            return Math.max(this.firstTextLength, this.secondTextLength);
        }
    }
}
