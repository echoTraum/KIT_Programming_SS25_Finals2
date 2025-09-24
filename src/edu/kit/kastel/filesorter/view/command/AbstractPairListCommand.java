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
 *
 * @author ugsrv
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
        return "%s-%s: %s".formatted(summary.firstIdentifier(), summary.secondIdentifier(),
                this.metric.format(metricValue));
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
                int firstTokenCount = tokenizedTexts.get(firstIdentifier).size();
                int secondTokenCount = tokenizedTexts.get(secondIdentifier).size();
                accumulators.put(new PairKey(firstIdentifier, secondIdentifier),
                        new PairAccumulator(firstIdentifier, secondIdentifier, firstTokenCount, secondTokenCount));
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
        private final int firstTokenCount;
        private final int secondTokenCount;
        private int totalMatchLength;
        private int longestMatchLength;

        PairAccumulator(String firstIdentifier, String secondIdentifier, int firstTokenCount, int secondTokenCount) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.firstTokenCount = firstTokenCount;
            this.secondTokenCount = secondTokenCount;
        }

        void addMatch(AnalysisMatch match) {
            this.totalMatchLength += match.length();
            this.longestMatchLength = Math.max(this.longestMatchLength, match.length());
        }

        PairSummary toSummary() {
            return new PairSummary(this.firstIdentifier, this.secondIdentifier, this.firstTokenCount,
                    this.secondTokenCount, this.totalMatchLength, this.longestMatchLength);
        }
    }
}
