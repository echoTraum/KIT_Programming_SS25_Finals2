package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.AnalysisMatch;
import edu.kit.kastel.filesorter.model.AnalysisResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility responsible for collecting {@link PairSummary} instances for an analysis result.
 */
final class PairSummaryCollector {

    private PairSummaryCollector() {
        // utility class
    }

    static List<PairSummary> collectSummaries(AnalysisResult analysisResult) {
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

