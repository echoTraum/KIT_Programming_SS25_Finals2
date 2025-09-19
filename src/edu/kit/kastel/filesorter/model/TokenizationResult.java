package edu.kit.kastel.filesorter.model;

import java.util.List;
import java.util.Objects;

/**
 * Result of a tokenization operation executed by the model.
 *
 * <p>A result either contains the successfully generated tokens or provides an error message
 * explaining why tokenization could not be performed.</p>
 *
 * @param tokens the produced tokens (empty when an error occurred)
 * @param errorMessage the error message in case of a failure; {@code null} for successful results
 */
public record TokenizationResult(List<String> tokens, String errorMessage) {

    /**
     * Creates a successful tokenization result.
     *
     * @param tokens the generated tokens
     * @return the success result
     */
    public static TokenizationResult success(List<String> tokens) {
        return new TokenizationResult(List.copyOf(tokens), null);
    }

    /**
     * Creates a failing tokenization result.
     *
     * @param errorMessage the error description
     * @return the failure result
     */
    public static TokenizationResult error(String errorMessage) {
        return new TokenizationResult(List.of(), Objects.requireNonNull(errorMessage));
    }

    /**
     * Returns whether the tokenization was successful.
     *
     * @return {@code true} if the tokenization succeeded, {@code false} otherwise
     */
    public boolean isSuccess() {
        return this.errorMessage == null;
    }
}
