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
 * produced matches and tokenization. Instances of this class are immutable.</p>
 *
 * @author ugsrv
 */
public final class AnalysisResult {

    private static final String ERROR_INVALID_MIN_MATCH_LENGTH = "minMatchLength must be positive.";

    private final TokenizationStrategy strategy;
    private final int minMatchLength;
    private final Map<String, List<String>> tokenizedTexts;
    private final List<AnalysisMatch> matches;

    /**
     * Constructs an immutable object representing the result of a text analysis.
     *
     * @param strategy the tokenization strategy used for splitting texts into tokens
     * @param minMatchLength the minimum number of tokens a match must contain to be included in the result
     * @param tokenizedTexts a map containing the tokenized representations of analyzed texts,
     *                       where keys are identifiers for the texts and values are the lists of tokens
     * @param matches a list of matches found during text analysis
     * @throws NullPointerException if {@code strategy}, {@code tokenizedTexts}, or {@code matches} is {@code null}
     * @throws IllegalArgumentException if {@code minMatchLength} is less than 1
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
