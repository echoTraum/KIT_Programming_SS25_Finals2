package edu.kit.kastel.filesorter.view.command;

/**
 * Summarizes the analysis statistics for a pair of texts.
 *
 * @param firstIdentifier the identifier of the first text
 * @param secondIdentifier the identifier of the second text
 * @param firstTokenCount the number of tokens of the first text
 * @param secondTokenCount the number of tokens of the second text
 * @param totalMatchLength the sum of the lengths of all matches
 * @param longestMatchLength the length of the longest match
 */
record PairSummary(String firstIdentifier, String secondIdentifier,
                   int firstTokenCount, int secondTokenCount,
                   int totalMatchLength, int longestMatchLength) {

    double symmetricSimilarity() {
        int combinedLength = this.firstTokenCount + this.secondTokenCount;
        if (combinedLength == 0 || this.totalMatchLength == 0) {
            return 0;
        }
        return (2.0 * this.totalMatchLength) / combinedLength;
    }

    double maximumSimilarity() {
        return Math.max(similarityToFirst(), similarityToSecond());
    }

    double minimumSimilarity() {
        return Math.min(similarityToFirst(), similarityToSecond());
    }

    private double similarityToFirst() {
        return similarity(this.firstTokenCount);
    }

    private double similarityToSecond() {
        return similarity(this.secondTokenCount);
    }

    private double similarity(int tokenCount) {
        if (tokenCount == 0 || this.totalMatchLength == 0) {
            return 0;
        }
        return (double) this.totalMatchLength / tokenCount;
    }
}
