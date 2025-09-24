package edu.kit.kastel.filesorter.view.command;

/**
 * Summarizes the analysis statistics for a pair of texts.
 *
 * @param firstIdentifier the identifier of the first text
 * @param secondIdentifier the identifier of the second text
 * @param matchCount the number of matches detected between both texts
 * @param totalMatchLength the sum of the lengths of all matches
 * @param longestMatchLength the length of the longest match
 * @param firstCoverage the portion of tokens of the first text that participate in a match
 * @param secondCoverage the portion of tokens of the second text that participate in a match
 */
record PairSummary(String firstIdentifier, String secondIdentifier,
                   int matchCount, int totalMatchLength, int longestMatchLength,
                   double firstCoverage, double secondCoverage) {

    double averageMatchLength() {
        return this.matchCount == 0 ? 0 : (double) this.totalMatchLength / this.matchCount;
    }

    double minimumCoverage() {
        return Math.min(this.firstCoverage, this.secondCoverage);
    }

    double maximumCoverage() {
        return Math.max(this.firstCoverage, this.secondCoverage);
    }

    double averageCoverage() {
        return (this.firstCoverage + this.secondCoverage) / 2.0;
    }
}
