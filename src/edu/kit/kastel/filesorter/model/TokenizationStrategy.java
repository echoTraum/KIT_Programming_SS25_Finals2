package edu.kit.kastel.filesorter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Enumeration of supported tokenization strategies.
 *
 * <p>Each strategy defines how a text should be split into tokens that can later be reused for
 * matching or inspection purposes. The strategies are implemented as follows:
 * <ul>
 *     <li>{@link #CHAR}: splits the text into single Unicode characters.</li>
 *     <li>{@link #WORD}: splits the text into words separated by whitespace. Punctuation marks
 *     remain attached to the surrounding words.</li>
 *     <li>{@link #SMART}: splits the text into words while treating punctuation marks (except
 *     apostrophes and hyphen-minus characters occurring between letters or digits) as separate
 *     tokens.</li>
 * </ul>
 * </p>
 */
public enum TokenizationStrategy {
    /**
     * Tokenization strategy that returns every character of the input text as a separate token.
     */
    CHAR {
        @Override
        public List<String> tokenize(String text) {
            Objects.requireNonNull(text);
            List<String> tokens = new ArrayList<>(text.length());
            text.codePoints().forEach(codePoint -> tokens.add(new String(Character.toChars(codePoint))));
            return tokens;
        }
    },
    /**
     * Tokenization strategy that splits the text based on whitespace characters.
     */
    WORD {
        private static final String WORD_DELIMITER_REGEX = "\\s+";

        @Override
        public List<String> tokenize(String text) {
            Objects.requireNonNull(text);
            String[] split = text.split(WORD_DELIMITER_REGEX);
            List<String> tokens = new ArrayList<>(split.length);
            for (String token : split) {
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
            return tokens;
        }
    },
    /**
     * Tokenization strategy that splits the text into words while keeping punctuation separate.
     */
    SMART {
        private static final char APOSTROPHE = '\'';
        private static final char HYPHEN_MINUS = '-';

        @Override
        public List<String> tokenize(String text) {
            Objects.requireNonNull(text);
            List<String> tokens = new ArrayList<>();
            StringBuilder currentToken = new StringBuilder();

            for (int index = 0; index < text.length(); index++) {
                char current = text.charAt(index);
                if (Character.isWhitespace(current)) {
                    flushToken(tokens, currentToken);
                    continue;
                }
                if (Character.isLetterOrDigit(current) || isWordConnector(text, index)) {
                    currentToken.append(current);
                    continue;
                }
                flushToken(tokens, currentToken);
                tokens.add(String.valueOf(current));
            }
            flushToken(tokens, currentToken);
            return tokens;
        }

        private static boolean isWordConnector(String text, int index) {
            char connector = text.charAt(index);
            if (connector != APOSTROPHE && connector != HYPHEN_MINUS) {
                return false;
            }
            if (index == 0 || index >= text.length() - 1) {
                return false;
            }
            char previous = text.charAt(index - 1);
            char next = text.charAt(index + 1);
            return Character.isLetterOrDigit(previous) && Character.isLetterOrDigit(next);
        }

        private static void flushToken(List<String> tokens, StringBuilder currentToken) {
            if (currentToken.length() > 0) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
            }
        }
    };

    /**
     * Tokenizes the provided text.
     *
     * @param text the text to tokenize
     * @return the tokens produced by the strategy
     */
    public abstract List<String> tokenize(String text);

    /**
     * Parses the provided string into a {@link TokenizationStrategy}. Parsing is case insensitive
     * and ignores surrounding whitespace.
     *
     * @param value the string representation of the strategy
     * @return the matching strategy
     * @throws IllegalArgumentException if no matching strategy exists
     */
    public static TokenizationStrategy fromName(String value) {
        Objects.requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (TokenizationStrategy strategy : values()) {
            if (strategy.name().equals(normalized)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown tokenization strategy: " + value);
    }

}
