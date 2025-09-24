package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.AnalysisMatch;
import edu.kit.kastel.filesorter.model.AnalysisResult;
import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Base class shared by commands that list program pairs after an analysis.
 */
abstract class AbstractPairListCommand implements Command<SequenceMatcher> {

    private static final String ERROR_NO_ANALYSIS_RESULT = "No analysis result available.";
    private static final String MESSAGE_NO_PROGRAM_PAIRS = "No program pairs available.";

    private final ListMetric metric;
    private final SortOrder order;
    private final OptionalInt limit;

    AbstractPairListCommand(ListMetric metric, SortOrder order, OptionalInt limit) {
        this.metric = metric;
        this.order = order;
        this.limit = limit;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        AnalysisResult analysisResult = handle.getLastAnalysisResult();
        if (analysisResult == null) {
            return Result.error(ERROR_NO_ANALYSIS_RESULT);
        }

        List<PairSummary> summaries = collectSummaries(analysisResult);
        if (summaries.isEmpty()) {
            return Result.success(MESSAGE_NO_PROGRAM_PAIRS);
        }

        Comparator<PairSummary> comparator = Comparator.comparingDouble(this.metric::extract);
        if (this.order == SortOrder.DESCENDING) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(PairSummary::firstIdentifier)
                .thenComparing(PairSummary::secondIdentifier);

        summaries.sort(comparator);
        return Result.success(formatSummaries(summaries));
    }

    private String formatSummaries(List<PairSummary> summaries) {
        int maximum = this.limit.orElse(Integer.MAX_VALUE);
        StringBuilder builder = new StringBuilder();
        int processed = 0;
        for (PairSummary summary : summaries) {
            if (processed >= maximum) {
                break;
            }
            if (processed > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(formatSummary(summary));
            processed++;
        }
        return builder.toString();
    }

    private String formatSummary(PairSummary summary) {
        double metricValue = this.metric.extract(summary);
        return "%s ~ %s ~ metric=%s:%s ~ matches=%d ~ total=%d ~ longest=%d ~ average=%s ~ coverage_first=%s ~ coverage_second=%s"
                .formatted(summary.firstIdentifier(), summary.secondIdentifier(), this.metric.displayName(),
                        this.metric.format(metricValue), summary.matchCount(), summary.totalMatchLength(),
                        summary.longestMatchLength(), NumberFormatUtil.formatDecimal(summary.averageMatchLength()),
                        NumberFormatUtil.formatDecimal(summary.firstCoverage()),
                        NumberFormatUtil.formatDecimal(summary.secondCoverage()));
    }

    private static List<PairSummary> collectSummaries(AnalysisResult analysisResult) {
        Map<String, List<String>> tokenizedTexts = analysisResult.tokenizedTexts();
        List<String> identifiers = new ArrayList<>(tokenizedTexts.keySet());
        if (identifiers.size() < 2) {
            return List.of();
        }

        Map<PairKey, PairAccumulator> accumulators = new LinkedHashMap<>();
        for (int firstIndex = 0; firstIndex < identifiers.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < identifiers.size(); secondIndex++) {
                String firstIdentifier = identifiers.get(firstIndex);
                String secondIdentifier = identifiers.get(secondIndex);
                int firstTokens = tokenizedTexts.get(firstIdentifier).size();
                int secondTokens = tokenizedTexts.get(secondIdentifier).size();
                accumulators.put(new PairKey(firstIdentifier, secondIdentifier),
                        new PairAccumulator(firstIdentifier, secondIdentifier, firstTokens, secondTokens));
            }
        }

        for (AnalysisMatch match : analysisResult.matches()) {
            PairKey key = new PairKey(match.firstIdentifier(), match.secondIdentifier());
            PairAccumulator accumulator = accumulators.get(key);
            if (accumulator != null) {
                accumulator.addMatch(match);
            }
        }

        List<PairSummary> summaries = new ArrayList<>(accumulators.size());
        for (PairAccumulator accumulator : accumulators.values()) {
            summaries.add(accumulator.toSummary());
        }
        return summaries;
    }

    private record PairKey(String firstIdentifier, String secondIdentifier) {
    }

    private static final class PairAccumulator {
        private final String firstIdentifier;
        private final String secondIdentifier;
        private final boolean[] firstCoverage;
        private final boolean[] secondCoverage;
        private int matchCount;
        private int totalLength;
        private int longestMatch;

        PairAccumulator(String firstIdentifier, String secondIdentifier, int firstTokens, int secondTokens) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.firstCoverage = new boolean[firstTokens];
            this.secondCoverage = new boolean[secondTokens];
        }

        void addMatch(AnalysisMatch match) {
            this.matchCount++;
            this.totalLength += match.length();
            this.longestMatch = Math.max(this.longestMatch, match.length());
            markCovered(this.firstCoverage, match.firstIndex(), match.length());
            markCovered(this.secondCoverage, match.secondIndex(), match.length());
        }

        PairSummary toSummary() {
            return new PairSummary(this.firstIdentifier, this.secondIdentifier, this.matchCount, this.totalLength,
                    this.longestMatch, coverageRatio(this.firstCoverage), coverageRatio(this.secondCoverage));
        }

        private static void markCovered(boolean[] coverage, int start, int length) {
            for (int index = 0; index < length; index++) {
                int position = start + index;
                if (position >= 0 && position < coverage.length) {
                    coverage[position] = true;
                }
            }
        }

        private static double coverageRatio(boolean[] coverage) {
            if (coverage.length == 0) {
                return 0;
            }
            int coveredTokens = 0;
            for (boolean value : coverage) {
                if (value) {
                    coveredTokens++;
                }
            }
            return (double) coveredTokens / coverage.length;
        }
    }
}
