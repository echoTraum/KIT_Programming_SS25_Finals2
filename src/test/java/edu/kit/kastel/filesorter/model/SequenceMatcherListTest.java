package edu.kit.kastel.filesorter.model;

import edu.kit.kastel.filesorter.view.Result;
import edu.kit.kastel.filesorter.view.ResultType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceMatcherListTest {

    private SequenceMatcher matcher;

    @BeforeEach
    void setUp() {
        this.matcher = new SequenceMatcher();
    }

    @Test
    void listWithoutAnalysisReturnsError() {
        Result result = this.matcher.list(ListMetric.AVG, SortOrder.DESC);

        assertEquals(ResultType.FAILURE, result.getType());
        assertEquals("No analysis result available.", result.getMessage());
    }

    @Test
    void averageMetricIsFormattedAndSortedDescending() {
        AnalysisResult result = performAnalysis();

        Result listResult = this.matcher.list(ListMetric.AVG, SortOrder.DESC);

        assertEquals(ResultType.SUCCESS, listResult.getType());
        assertEquals(expectedMessage(result, ListMetric.AVG, SortOrder.DESC), listResult.getMessage());
    }

    @Test
    void lengthMetricAscendingUsesIdentifierTieBreakers() {
        AnalysisResult result = performAnalysis();

        Result listResult = this.matcher.list(ListMetric.LEN, SortOrder.ASC);

        assertEquals(ResultType.SUCCESS, listResult.getType());
        assertEquals(expectedMessage(result, ListMetric.LEN, SortOrder.ASC), listResult.getMessage());
    }

    private AnalysisResult performAnalysis() {
        this.matcher.input("alpha.txt", "alpha beta gamma beta alpha");
        this.matcher.input("beta.txt", "alpha beta delta beta alpha");
        this.matcher.input("gamma.txt", "alpha gamma gamma beta alpha");

        this.matcher.analyze(TokenizationStrategy.WORD, 1);
        return this.matcher.getLastAnalysisResult();
    }

    private static String expectedMessage(AnalysisResult analysisResult, ListMetric metric, SortOrder order) {
        Map<Pair, Stats> stats = collectStatistics(analysisResult);
        List<Stats> values = new ArrayList<>(stats.values());
        values.sort((first, second) -> {
            int comparison = Double.compare(metricValue(second, metric), metricValue(first, metric));
            if (order == SortOrder.ASC) {
                comparison = -comparison;
            }
            if (comparison != 0) {
                return comparison;
            }
            int firstIdentifierComparison = first.firstIdentifier().compareTo(second.firstIdentifier());
            if (firstIdentifierComparison != 0) {
                return firstIdentifierComparison;
            }
            return first.secondIdentifier().compareTo(second.secondIdentifier());
        });

        List<String> lines = new ArrayList<>();
        for (Stats stat : values) {
            lines.add(stat.firstIdentifier() + "-" + stat.secondIdentifier() + ": " + formatMetric(stat, metric));
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static Map<Pair, Stats> collectStatistics(AnalysisResult analysisResult) {
        Map<String, List<String>> tokens = analysisResult.tokenizedTexts();
        List<String> identifiers = new ArrayList<>(tokens.keySet());
        Map<Pair, Stats> stats = new LinkedHashMap<>();

        for (int firstIndex = 0; firstIndex < identifiers.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < identifiers.size(); secondIndex++) {
                String firstIdentifier = identifiers.get(firstIndex);
                String secondIdentifier = identifiers.get(secondIndex);
                stats.put(new Pair(firstIdentifier, secondIdentifier),
                        new Stats(firstIdentifier, secondIdentifier, tokens.get(firstIdentifier).size(),
                                tokens.get(secondIdentifier).size()));
            }
        }

        for (AnalysisMatch match : analysisResult.matches()) {
            Pair key = new Pair(match.firstIdentifier(), match.secondIdentifier());
            Stats statistics = stats.get(key);
            if (statistics == null) {
                statistics = new Stats(match.firstIdentifier(), match.secondIdentifier(),
                        tokens.get(match.firstIdentifier()).size(), tokens.get(match.secondIdentifier()).size());
                stats.put(key, statistics);
            }
            statistics.addMatch(match.length());
        }

        return stats;
    }

    private static double metricValue(Stats stats, ListMetric metric) {
        return switch (metric) {
            case AVG -> stats.averageLength();
            case MAX -> stats.maxLength();
            case MIN -> stats.minLength();
            case LONG -> stats.longerTextLength();
            case LEN -> stats.totalMatchLength();
        };
    }

    private static String formatMetric(Stats stats, ListMetric metric) {
        return switch (metric) {
            case AVG -> String.format(Locale.ROOT, "%.2f", stats.averageLength());
            case MAX -> Long.toString(stats.maxLength());
            case MIN -> Long.toString(stats.minLength());
            case LONG -> Long.toString(stats.longerTextLength());
            case LEN -> Long.toString(stats.totalMatchLength());
        };
    }

    private record Pair(String firstIdentifier, String secondIdentifier) {
    }

    private static final class Stats {
        private final String firstIdentifier;
        private final String secondIdentifier;
        private final int firstLength;
        private final int secondLength;
        private long totalLength;
        private int matchCount;
        private int maxLength;
        private int minLength = Integer.MAX_VALUE;

        Stats(String firstIdentifier, String secondIdentifier, int firstLength, int secondLength) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.firstLength = firstLength;
            this.secondLength = secondLength;
        }

        void addMatch(int length) {
            this.totalLength += length;
            this.matchCount++;
            if (length > this.maxLength) {
                this.maxLength = length;
            }
            if (length < this.minLength) {
                this.minLength = length;
            }
        }

        String firstIdentifier() {
            return this.firstIdentifier;
        }

        String secondIdentifier() {
            return this.secondIdentifier;
        }

        double averageLength() {
            if (this.matchCount == 0) {
                return 0.0d;
            }
            return (double) this.totalLength / this.matchCount;
        }

        long maxLength() {
            return this.matchCount == 0 ? 0L : this.maxLength;
        }

        long minLength() {
            return this.matchCount == 0 ? 0L : this.minLength;
        }

        long totalMatchLength() {
            return this.totalLength;
        }

        long longerTextLength() {
            return Math.max(this.firstLength, this.secondLength);
        }
    }
}

