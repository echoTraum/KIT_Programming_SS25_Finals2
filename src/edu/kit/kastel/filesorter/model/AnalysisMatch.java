package edu.kit.kastel.filesorter.model;

import java.util.Objects;

/**
 * Represents a contiguous matching token sequence between two texts.
 *
 * @param firstIdentifier the identifier of the first text containing the match
 * @param firstIndex the starting token index of the match within the first text
 * @param secondIdentifier the identifier of the second text containing the match
 * @param secondIndex the starting token index of the match within the second text
 * @param length the length of the match measured in tokens
 *
 * @author ugsrv
 */
public record AnalysisMatch(String firstIdentifier, int firstIndex,
        String secondIdentifier, int secondIndex, int length) {

    private static final String ERROR_FIRST_IDENTIFIER_NULL = "firstIdentifier must not be null.";
    private static final String ERROR_SECOND_IDENTIFIER_NULL = "secondIdentifier must not be null.";
    private static final String ERROR_NEGATIVE_FIRST_INDEX = "firstIndex must be non-negative.";
    private static final String ERROR_NEGATIVE_SECOND_INDEX = "secondIndex must be non-negative.";
    private static final String ERROR_NON_POSITIVE_LENGTH = "length must be positive.";

    /**
     * Creates a new match instance.
     *
     * @param firstIdentifier the identifier of the first text containing the match
     * @param firstIndex the starting token index of the match within the first text
     * @param secondIdentifier the identifier of the second text containing the match
     * @param secondIndex the starting token index of the match within the second text
     * @param length the length of the match measured in tokens
     */
    public AnalysisMatch {
        Objects.requireNonNull(firstIdentifier, ERROR_FIRST_IDENTIFIER_NULL);
        Objects.requireNonNull(secondIdentifier, ERROR_SECOND_IDENTIFIER_NULL);
        if (firstIndex < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE_FIRST_INDEX);
        }
        if (secondIndex < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE_SECOND_INDEX);
        }
        if (length < 1) {
            throw new IllegalArgumentException(ERROR_NON_POSITIVE_LENGTH);
        }
    }
}
