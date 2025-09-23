package edu.kit.kastel.filesorter.model;

import edu.kit.kastel.filesorter.util.PathValidator;
import edu.kit.kastel.filesorter.view.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents the application's model storing the texts that are available for comparison.
 *
 * <p>The model keeps track of all texts that have been loaded via the user interface. Each text is
 * identified by the file name of the source that has been loaded. A subsequent load using the same
 * identifier replaces the previously stored text.</p>
 *
 * @author ugsrv
 */
public class SequenceMatcher {

    private static final String ERROR_COULD_NOT_READ_FILE = "Could not read file.";
    private static final String MESSAGE_LOADED = "Loaded %s";
    private static final String MESSAGE_UPDATED = "Updated %s";
    private static final String ERROR_UNKNOWN_IDENTIFIER = "No text stored for identifier '%s'.";
    private static final String ERROR_MISSING_IDENTIFIER = "No identifier provided.";
    private static final String ERROR_MISSING_STRATEGY = "No tokenization strategy provided.";
    private static final String ERROR_INVALID_MIN_MATCH_LENGTH = "Minimum match length must be positive.";
    private static final String MESSAGE_ANALYSIS_TOOK = "Analysis took %dms";
    private static final String MESSAGE_CLEARED = "Cleared all texts.";

    private final Map<String, LoadedText> loadedTexts = new LinkedHashMap<>();
    private AnalysisResult lastAnalysisResult;

    /**
     * Loads the contents of the file located at the provided {@link Path}. The file name is used as
     * identifier for the loaded text. If a text with the same identifier already exists, it is
     * replaced by the new data.
     *
     * @param path the path of the file to read
     * @return the result of the loading operation
     */
    public Result load(Path path) {
        Objects.requireNonNull(path);
        return load(path.toString());
    }

    /**
     * Loads the contents of the file located at the provided path. Convenience overload accepting a
     * path represented as string.
     *
     * @param path the path of the file to read
     * @return the result of the loading operation
     */
    public Result load(String path) {
        Objects.requireNonNull(path);
        if (!PathValidator.isValid(path)) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        File file = new File(path);
        File canonicalFile;
        try {
            canonicalFile = file.getCanonicalFile();
        } catch (IOException e) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        if (!canonicalFile.isFile()) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        String content;
        try (FileInputStream inputStream = new FileInputStream(canonicalFile)) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        return storeText(canonicalFile.getName(), canonicalFile.getPath(), content);
    }

    /**
     * Stores the provided text under the given identifier. If a text with the same identifier already
     * exists it is replaced by the new content.
     *
     * @param identifier the identifier to store the text under
     * @param text the text to store
     * @return the result of the operation
     */
    public Result input(String identifier, String text) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(text);

        return storeText(identifier, null, text);
    }

    /**
     * Tokenizes the stored text identified by the provided identifier using the given strategy.
     *
     * @param identifier the identifier of the text to tokenize
     * @param strategy the tokenization strategy to apply
     * @return the result of the tokenization attempt
     */
    public TokenizationResult tokenize(String identifier, TokenizationStrategy strategy) {
        if (identifier == null) {
            return TokenizationResult.error(ERROR_MISSING_IDENTIFIER);
        }
        if (strategy == null) {
            return TokenizationResult.error(ERROR_MISSING_STRATEGY);
        }

        LoadedText loadedText = this.loadedTexts.get(identifier);
        if (loadedText == null) {
            return TokenizationResult.error(ERROR_UNKNOWN_IDENTIFIER.formatted(identifier));
        }

        List<String> tokens = strategy.tokenize(loadedText.content());
        return TokenizationResult.success(tokens);
    }

    /**
     * Executes a text analysis on all loaded texts using the provided strategy and minimum match length.
     *
     * @param strategy the strategy to use for tokenizing the texts prior to analysis
     * @param minMatchLength the minimum length of a match measured in tokens
     * @return the result of the analysis
     */
    public Result analyze(TokenizationStrategy strategy, int minMatchLength) {
        if (strategy == null) {
            return Result.error(ERROR_MISSING_STRATEGY);
        }
        if (minMatchLength < 1) {
            return Result.error(ERROR_INVALID_MIN_MATCH_LENGTH);
        }

        long startTime = System.nanoTime();

        Map<String, List<String>> tokenizedTexts = new LinkedHashMap<>();
        for (LoadedText loadedText : this.loadedTexts.values()) {
            tokenizedTexts.put(loadedText.identifier(), strategy.tokenize(loadedText.content()));
        }

        List<AnalysisMatch> matches = collectMatches(tokenizedTexts, minMatchLength);
        this.lastAnalysisResult = new AnalysisResult(strategy, minMatchLength, tokenizedTexts, matches);

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        return Result.success(MESSAGE_ANALYSIS_TOOK.formatted(durationMs));
    }

    /**
     * Returns the result of the most recent analysis.
     *
     * @return the last analysis result or {@code null} if no analysis has been executed yet
     */
    public AnalysisResult getLastAnalysisResult() {
        return this.lastAnalysisResult;
    }

    /**
     * Removes all texts currently stored in this matcher.
     *
     * @return the result of the clear operation
     */
    public Result clear() {
        this.loadedTexts.clear();
        this.lastAnalysisResult = null;
        return Result.success(MESSAGE_CLEARED);
    }

    private static List<AnalysisMatch> collectMatches(Map<String, List<String>> tokenizedTexts, int minMatchLength) {
        List<AnalysisMatch> matches = new ArrayList<>();
        List<Entry<String, List<String>>> entries = new ArrayList<>(tokenizedTexts.entrySet());
        for (int firstTextIndex = 0; firstTextIndex < entries.size(); firstTextIndex++) {
            for (int secondTextIndex = firstTextIndex + 1; secondTextIndex < entries.size(); secondTextIndex++) {
                matches.addAll(findMatches(entries.get(firstTextIndex), entries.get(secondTextIndex), minMatchLength));
            }
        }
        return matches;
    }

    private static List<AnalysisMatch> findMatches(Entry<String, List<String>> firstEntry,
            Entry<String, List<String>> secondEntry, int minMatchLength) {
        List<AnalysisMatch> matches = new ArrayList<>();
        List<String> firstTokens = firstEntry.getValue();
        List<String> secondTokens = secondEntry.getValue();
        for (int firstIndex = 0; firstIndex < firstTokens.size(); firstIndex++) {
            for (int secondIndex = 0; secondIndex < secondTokens.size(); secondIndex++) {
                int matchLength = determineMatchLength(firstTokens, secondTokens, firstIndex, secondIndex);
                if (matchLength >= minMatchLength
                        && isStartOfMatch(firstTokens, secondTokens, firstIndex, secondIndex)) {
                    matches.add(new AnalysisMatch(firstEntry.getKey(), firstIndex,
                            secondEntry.getKey(), secondIndex, matchLength));
                }
            }
        }
        return matches;
    }

    private static int determineMatchLength(List<String> firstTokens, List<String> secondTokens, int firstIndex,
            int secondIndex) {
        int length = 0;
        while (firstIndex + length < firstTokens.size()
                && secondIndex + length < secondTokens.size()
                && firstTokens.get(firstIndex + length).equals(secondTokens.get(secondIndex + length))) {
            length++;
           }
        return length;
    }

    private static boolean isStartOfMatch(List<String> firstTokens, List<String> secondTokens, int firstIndex,
            int secondIndex) {
        return firstIndex == 0 || secondIndex == 0 || !firstTokens.get(firstIndex - 1).equals(secondTokens.get(secondIndex - 1));
    }

    private Result storeText(String identifier, String source, String content) {
        boolean wasPresent = this.loadedTexts.containsKey(identifier);
        this.loadedTexts.put(identifier, new LoadedText(identifier, source, content));

        return Result.success((wasPresent ? MESSAGE_UPDATED : MESSAGE_LOADED).formatted(identifier));
    }

    private record LoadedText(String identifier, String source, String content) {
    }
}
