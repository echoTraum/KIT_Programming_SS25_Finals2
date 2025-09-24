package edu.kit.kastel.filesorter.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides editing facilities for the matches between two analysed texts.
 */
public final class ComparisonEditor {

    private static final String ERROR_INVALID_MATCH_INDEX = "Invalid match index.";
    private static final String ERROR_LENGTH_NOT_POSITIVE = "Length must be positive.";
    private static final String ERROR_LENGTH_ZERO = "Length must not be zero.";
    private static final String ERROR_MATCH_OUT_OF_BOUNDS = "Match would exceed text boundaries.";
    private static final String ERROR_TRUNCATE_TOO_LONG = "Match cannot be truncated completely.";
    private static final String ERROR_TOKENS_MISMATCH = "Tokens do not match in the selected range.";
    private static final String ERROR_CONTEXT_NEGATIVE = "Context size must be non-negative.";

    private final SequenceMatcher matcher;
    private final String firstIdentifier;
    private final String secondIdentifier;
    private final String analysisFirstIdentifier;
    private final String analysisSecondIdentifier;
    private final boolean swappedOrientation;
    private final List<String> firstTokens;
    private final List<String> secondTokens;
    private final List<EditableMatch> matches = new ArrayList<>();

    ComparisonEditor(SequenceMatcher matcher, AnalysisResult analysisResult,
            String firstIdentifier, String secondIdentifier) {
        this.matcher = Objects.requireNonNull(matcher);
        this.firstIdentifier = Objects.requireNonNull(firstIdentifier);
        this.secondIdentifier = Objects.requireNonNull(secondIdentifier);

        Map<String, List<String>> tokenizedTexts = analysisResult.tokenizedTexts();
        this.firstTokens = List.copyOf(tokenizedTexts.get(this.firstIdentifier));
        this.secondTokens = List.copyOf(tokenizedTexts.get(this.secondIdentifier));

        List<String> identifiersInOrder = new ArrayList<>(tokenizedTexts.keySet());
        int indexFirst = identifiersInOrder.indexOf(this.firstIdentifier);
        int indexSecond = identifiersInOrder.indexOf(this.secondIdentifier);
        if (indexFirst <= indexSecond) {
            this.analysisFirstIdentifier = this.firstIdentifier;
            this.analysisSecondIdentifier = this.secondIdentifier;
            this.swappedOrientation = false;
        } else {
            this.analysisFirstIdentifier = this.secondIdentifier;
            this.analysisSecondIdentifier = this.firstIdentifier;
            this.swappedOrientation = true;
        }

        for (AnalysisMatch match : analysisResult.matches()) {
            if (involvesPair(match)) {
                int displayFirstIndex;
                int displaySecondIndex;
                if (match.firstIdentifier().equals(this.firstIdentifier)) {
                    displayFirstIndex = match.firstIndex();
                    displaySecondIndex = match.secondIndex();
                } else {
                    displayFirstIndex = match.secondIndex();
                    displaySecondIndex = match.firstIndex();
                }
                this.matches.add(new EditableMatch(displayFirstIndex, displaySecondIndex, match.length()));
            }
        }

        sortMatches();
    }

    private boolean involvesPair(AnalysisMatch match) {
        return (match.firstIdentifier().equals(this.firstIdentifier)
                && match.secondIdentifier().equals(this.secondIdentifier))
                || (match.firstIdentifier().equals(this.secondIdentifier)
                        && match.secondIdentifier().equals(this.firstIdentifier));
    }

    private void sortMatches() {
        this.matches.sort(Comparator.comparingInt(EditableMatch::firstIndex)
                .thenComparingInt(EditableMatch::secondIndex));
    }

    /**
     * Returns the identifier of the first text as seen by the editor.
     *
     * @return the identifier of the first text
     */
    public String firstIdentifier() {
        return this.firstIdentifier;
    }

    /**
     * Returns the identifier of the second text as seen by the editor.
     *
     * @return the identifier of the second text
     */
    public String secondIdentifier() {
        return this.secondIdentifier;
    }

    /**
     * Returns the number of matches currently stored for the comparison.
     *
     * @return the number of matches
     */
    public int matchCount() {
        return this.matches.size();
    }

    /**
     * Returns an immutable view of the matches ordered by their starting position in the first text.
     *
     * @return the matches of the comparison
     */
    public List<MatchView> getMatches() {
        List<MatchView> views = new ArrayList<>(this.matches.size());
        for (EditableMatch match : this.matches) {
            views.add(match.toView());
        }
        return List.copyOf(views);
    }

    /**
     * Returns the context information for the specified match.
     *
     * @param matchNumber the one-based match index
     * @param contextSize the number of context tokens to include on each side
     * @return the context information for the requested match
     * @throws ComparisonEditingException if the arguments are invalid
     */
    public MatchContext contextForMatch(int matchNumber, int contextSize) throws ComparisonEditingException {
        if (contextSize < 0) {
            throw new ComparisonEditingException(ERROR_CONTEXT_NEGATIVE);
        }
        EditableMatch match = retrieveMatch(matchNumber);
        return buildContext(match, contextSize);
    }

    private MatchContext buildContext(EditableMatch match, int contextSize) {
        int firstContextStart = Math.max(0, match.firstIndex() - contextSize);
        int firstContextEnd = Math.min(this.firstTokens.size(), match.firstIndex() + match.length() + contextSize);
        List<String> firstContext = new ArrayList<>(firstContextEnd - firstContextStart);
        for (int i = firstContextStart; i < firstContextEnd; i++) {
            firstContext.add(this.firstTokens.get(i));
        }
        int firstRelativeStart = match.firstIndex() - firstContextStart;

        int secondContextStart = Math.max(0, match.secondIndex() - contextSize);
        int secondContextEnd = Math.min(this.secondTokens.size(), match.secondIndex() + match.length() + contextSize);
        List<String> secondContext = new ArrayList<>(secondContextEnd - secondContextStart);
        for (int i = secondContextStart; i < secondContextEnd; i++) {
            secondContext.add(this.secondTokens.get(i));
        }
        int secondRelativeStart = match.secondIndex() - secondContextStart;

        return new MatchContext(List.copyOf(firstContext), firstRelativeStart,
                List.copyOf(secondContext), secondRelativeStart, match.length());
    }

    /**
     * Adds a new match to the comparison.
     *
     * @param firstIndex the starting token index within the first text
     * @param secondIndex the starting token index within the second text
     * @param length the length of the match in tokens
     * @throws ComparisonEditingException if the provided data does not describe a valid match
     */
    public void addMatch(int firstIndex, int secondIndex, int length) throws ComparisonEditingException {
        if (length < 1) {
            throw new ComparisonEditingException(ERROR_LENGTH_NOT_POSITIVE);
        }
        ensureRange(firstIndex, length, this.firstTokens.size());
        ensureRange(secondIndex, length, this.secondTokens.size());
        ensureTokensMatch(firstIndex, secondIndex, length);
        this.matches.add(new EditableMatch(firstIndex, secondIndex, length));
        persistMatches();
    }

    /**
     * Removes the specified match from the comparison.
     *
     * @param matchNumber the one-based index of the match to remove
     * @throws ComparisonEditingException if the index is invalid
     */
    public void discardMatch(int matchNumber) throws ComparisonEditingException {
        int internalIndex = toInternalIndex(matchNumber);
        this.matches.remove(internalIndex);
        persistMatches();
    }

    /**
     * Extends the specified match by the provided number of tokens.
     *
     * @param matchNumber the one-based index of the match to extend
     * @param delta the number of tokens to extend by; positive values extend the end, negative
     *              values extend the beginning
     * @throws ComparisonEditingException if the operation is not possible
     */
    public void extendMatch(int matchNumber, int delta) throws ComparisonEditingException {
        if (delta == 0) {
            throw new ComparisonEditingException(ERROR_LENGTH_ZERO);
        }
        EditableMatch match = retrieveMatch(matchNumber);
        if (delta > 0) {
            ensureRange(match.firstIndex() + match.length(), delta, this.firstTokens.size());
            ensureRange(match.secondIndex() + match.length(), delta, this.secondTokens.size());
            ensureTokensMatch(match.firstIndex() + match.length(), match.secondIndex() + match.length(), delta);
            match.increaseLength(delta);
        } else {
            int extension = -delta;
            if (match.firstIndex() < extension || match.secondIndex() < extension) {
                throw new ComparisonEditingException(ERROR_MATCH_OUT_OF_BOUNDS);
            }
            ensureTokensMatch(match.firstIndex() - extension, match.secondIndex() - extension, extension);
            match.shiftStart(-extension);
            match.increaseLength(extension);
        }
        persistMatches();
    }

    /**
     * Truncates the specified match by the provided number of tokens.
     *
     * @param matchNumber the one-based index of the match to truncate
     * @param delta the number of tokens to remove; positive values remove tokens from the start,
     *              negative values remove tokens from the end
     * @throws ComparisonEditingException if the operation would remove the entire match
     */
    public void truncateMatch(int matchNumber, int delta) throws ComparisonEditingException {
        if (delta == 0) {
            throw new ComparisonEditingException(ERROR_LENGTH_ZERO);
        }
        EditableMatch match = retrieveMatch(matchNumber);
        int reduction = Math.abs(delta);
        if (reduction >= match.length()) {
            throw new ComparisonEditingException(ERROR_TRUNCATE_TOO_LONG);
        }
        if (delta > 0) {
            match.shiftStart(delta);
            match.decreaseLength(reduction);
        } else {
            match.decreaseLength(reduction);
        }
        persistMatches();
    }

    private EditableMatch retrieveMatch(int matchNumber) throws ComparisonEditingException {
        int internalIndex = toInternalIndex(matchNumber);
        return this.matches.get(internalIndex);
    }

    private int toInternalIndex(int matchNumber) throws ComparisonEditingException {
        if (matchNumber < 1 || matchNumber > this.matches.size()) {
            throw new ComparisonEditingException(ERROR_INVALID_MATCH_INDEX);
        }
        return matchNumber - 1;
    }

    private void ensureRange(int index, int length, int tokenCount) throws ComparisonEditingException {
        if (index < 0 || length < 0 || index + length > tokenCount) {
            throw new ComparisonEditingException(ERROR_MATCH_OUT_OF_BOUNDS);
        }
    }

    private void ensureTokensMatch(int firstIndex, int secondIndex, int length) throws ComparisonEditingException {
        for (int offset = 0; offset < length; offset++) {
            if (!this.firstTokens.get(firstIndex + offset).equals(this.secondTokens.get(secondIndex + offset))) {
                throw new ComparisonEditingException(ERROR_TOKENS_MISMATCH);
            }
        }
    }

    private void persistMatches() {
        sortMatches();
        List<AnalysisMatch> replacements = new ArrayList<>(this.matches.size());
        for (EditableMatch match : this.matches) {
            int analysisFirstIndex = this.swappedOrientation ? match.secondIndex() : match.firstIndex();
            int analysisSecondIndex = this.swappedOrientation ? match.firstIndex() : match.secondIndex();
            replacements.add(new AnalysisMatch(this.analysisFirstIdentifier, analysisFirstIndex,
                    this.analysisSecondIdentifier, analysisSecondIndex, match.length()));
        }
        this.matcher.replaceMatchesForPair(this.analysisFirstIdentifier, this.analysisSecondIdentifier, replacements);
    }

    /**
     * Returns the total length of all matches measured in tokens.
     *
     * @return the combined length of all matches
     */
    public int totalMatchLength() {
        int total = 0;
        for (EditableMatch match : this.matches) {
            total += match.length();
        }
        return total;
    }

    /**
     * Returns the number of tokens contained in the first text.
     *
     * @return the number of tokens of the first text
     */
    public int firstTokenCount() {
        return this.firstTokens.size();
    }

    /**
     * Returns the number of tokens contained in the second text.
     *
     * @return the number of tokens of the second text
     */
    public int secondTokenCount() {
        return this.secondTokens.size();
    }

    /**
     * Immutable view of a match for presentation purposes.
     *
     * @param firstIndex the starting index in the first text
     * @param secondIndex the starting index in the second text
     * @param length the length of the match in tokens
     */
    public record MatchView(int firstIndex, int secondIndex, int length) {
    }

    /**
     * Context information for displaying a match with surrounding tokens.
     *
     * @param firstTokens the context tokens of the first text
     * @param firstMatchStart the index within {@code firstTokens} where the match starts
     * @param secondTokens the context tokens of the second text
     * @param secondMatchStart the index within {@code secondTokens} where the match starts
     * @param length the length of the match
     */
    public record MatchContext(List<String> firstTokens, int firstMatchStart,
            List<String> secondTokens, int secondMatchStart, int length) {
    }

    private static final class EditableMatch {
        private int firstIndex;
        private int secondIndex;
        private int length;

        EditableMatch(int firstIndex, int secondIndex, int length) {
            this.firstIndex = firstIndex;
            this.secondIndex = secondIndex;
            this.length = length;
        }

        int firstIndex() {
            return this.firstIndex;
        }

        int secondIndex() {
            return this.secondIndex;
        }

        int length() {
            return this.length;
        }

        void increaseLength(int delta) {
            this.length += delta;
        }

        void decreaseLength(int delta) {
            this.length -= delta;
        }

        void shiftStart(int delta) {
            this.firstIndex += delta;
            this.secondIndex += delta;
        }

        MatchView toView() {
            return new MatchView(this.firstIndex, this.secondIndex, this.length);
        }
    }
}

