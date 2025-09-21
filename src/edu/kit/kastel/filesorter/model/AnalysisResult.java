package edu.kit.kastel.filesorter.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of a text analysis.
 *
 * <p>The result stores the configuration that has been used for the analysis as well as the
 * produced matches and tokenizations. Instances of this class are immutable.</p>
 */
public final class AnalysisResult {

    private static final String ERROR_INVALID_MIN_MATCH_LENGTH = "minMatchLength must be positive.";

    private final TokenizationStrategy strategy;
    private final int minMatchLength;
    private final Map<String, List<String>> tokenizedTexts;
    private final List<AnalysisMatch> matches;

    /**
     * Creates a new analysis result.
     *
     * @param strategy the strategy that was used to tokenize the texts
     * @param minMatchLength the minimum length a match must have to be included in the result
     * @param tokenizedTexts the tokenized representation of all texts considered for the analysis
     * @param matches the matches that have been found between the texts
     */
    public AnalysisResult(TokenizationStrategy strategy, int minMatchLength,
            Map<String, List<String>> tokenizedTexts, List<AnalysisMatch> matches) {
        this.strategy = Objects.requireNonNull(strategy);
        if (minMatchLength < 1) {
            throw new IllegalArgumentException(ERROR_INVALID_MIN_MATCH_LENGTH);
        }
        Objects.requireNonNull(tokenizedTexts);
        Objects.requireNonNull(matches);

        Map<String, List<String>> tokenCopy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : tokenizedTexts.entrySet()) {
            tokenCopy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        this.minMatchLength = minMatchLength;
        this.tokenizedTexts = Collections.unmodifiableMap(tokenCopy);
        this.matches = List.copyOf(matches);
    }

    /**
     * Returns the strategy that was used for tokenizing the texts.
     *
     * @return the tokenization strategy used for the analysis
     */
    public TokenizationStrategy strategy() {
        return this.strategy;
    }

    /**
     * Returns the minimum number of tokens a match must contain to be included in the result.
     *
     * @return the minimum match length in tokens
     */
    public int minMatchLength() {
        return this.minMatchLength;
    }

    /**
     * Returns the tokenized representations of the analyzed texts.
     *
     * @return an unmodifiable view of the tokenized texts
     */
    public Map<String, List<String>> tokenizedTexts() {
        return this.tokenizedTexts;
    }

    /**
     * Returns the matches that have been found during the analysis.
     *
     * @return the matches produced by the analysis
     */
    public List<AnalysisMatch> matches() {
        return this.matches;
    }
}
