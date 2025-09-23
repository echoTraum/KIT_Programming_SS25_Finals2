package edu.kit.kastel.filesorter.model;

import edu.kit.kastel.filesorter.view.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Locale;
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
    private static final String ERROR_NO_ANALYSIS_RESULT = "No analysis result available.";
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
        Path normalizedPath;
        try {
            normalizedPath = path.toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        Path fileNamePath = normalizedPath.getFileName();
        if (fileNamePath == null) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        String identifier = fileNamePath.toString();
        String content;
        try {
            if (!Files.isRegularFile(normalizedPath)) {
                return Result.error(ERROR_COULD_NOT_READ_FILE);
            }
            content = Files.readString(normalizedPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }

        return storeText(identifier, normalizedPath, content);
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
        try {
            return load(Path.of(path));
        } catch (InvalidPathException e) {
            return Result.error(ERROR_COULD_NOT_READ_FILE);
        }
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

    /**
     * Lists statistics about the most recent analysis for each text pair using the provided metric
     * and ordering.
     *
     * @param metric the metric determining which statistic to display
     * @param order the order in which results should be returned
     * @return the formatted result of the listing operation or an error if no analysis result exists
     */
    public Result list(ListMetric metric, SortOrder order) {
        Objects.requireNonNull(metric);
        Objects.requireNonNull(order);

        if (this.lastAnalysisResult == null) {
            return Result.error(ERROR_NO_ANALYSIS_RESULT);
        }

        Map<String, List<String>> tokenizedTexts = this.lastAnalysisResult.tokenizedTexts();
        List<String> identifiers = new ArrayList<>(tokenizedTexts.keySet());
        if (identifiers.size() < 2) {
            return Result.success("");
        }

        Map<PairKey, PairStatistics> statistics = new LinkedHashMap<>();
        for (int firstIndex = 0; firstIndex < identifiers.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < identifiers.size(); secondIndex++) {
                String firstIdentifier = identifiers.get(firstIndex);
                String secondIdentifier = identifiers.get(secondIndex);
                statistics.put(new PairKey(firstIdentifier, secondIdentifier),
                        new PairStatistics(firstIdentifier, secondIdentifier,
                                tokenizedTexts.get(firstIdentifier).size(),
                                tokenizedTexts.get(secondIdentifier).size()));
            }
        }

        for (AnalysisMatch match : this.lastAnalysisResult.matches()) {
            PairKey key = new PairKey(match.firstIdentifier(), match.secondIdentifier());
            PairStatistics stats = statistics.get(key);
            if (stats == null) {
                List<String> firstTokens = tokenizedTexts.get(match.firstIdentifier());
                List<String> secondTokens = tokenizedTexts.get(match.secondIdentifier());
                if (firstTokens == null || secondTokens == null) {
                    continue;
                }
                stats = new PairStatistics(match.firstIdentifier(), match.secondIdentifier(),
                        firstTokens.size(), secondTokens.size());
                statistics.put(key, stats);
            }
            stats.addMatch(match.length());
        }

        List<PairMetricValue> values = new ArrayList<>();
        for (PairStatistics stats : statistics.values()) {
            values.add(new PairMetricValue(stats, computeMetricValue(stats, metric)));
        }

        Comparator<PairMetricValue> comparator = Comparator.comparingDouble(PairMetricValue::value);
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(value -> value.statistics().firstIdentifier())
                .thenComparing(value -> value.statistics().secondIdentifier());
        values.sort(comparator);

        List<String> lines = new ArrayList<>(values.size());
        for (PairMetricValue value : values) {
            lines.add(formatLine(value.statistics(), metric));
        }

        return Result.success(String.join(System.lineSeparator(), lines));
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
        for (; firstIndex + length < firstTokens.size() && secondIndex + length < secondTokens.size()
                     && firstTokens.get(firstIndex + length).equals(secondTokens.get(secondIndex + length)); length++) {
            length++;
        }
        return length;
    }

    private static boolean isStartOfMatch(List<String> firstTokens, List<String> secondTokens, int firstIndex,
            int secondIndex) {
        return firstIndex == 0 || secondIndex == 0 || !firstTokens.get(firstIndex - 1).equals(secondTokens.get(secondIndex - 1));
    }

    private static double computeMetricValue(PairStatistics statistics, ListMetric metric) {
        return switch (metric) {
            case AVG -> statistics.averageLength();
            case MAX -> statistics.maxLength();
            case MIN -> statistics.minLength();
            case LONG -> statistics.longerTextLength();
            case LEN -> statistics.totalMatchLength();
        };
    }

    private static String formatLine(PairStatistics statistics, ListMetric metric) {
        return statistics.firstIdentifier() + "-" + statistics.secondIdentifier() + ": "
                + formatMetric(statistics, metric);
    }

    private static String formatMetric(PairStatistics statistics, ListMetric metric) {
        return switch (metric) {
            case AVG -> formatDecimal(statistics.averageLength());
            case MAX -> formatInteger(statistics.maxLength());
            case MIN -> formatInteger(statistics.minLength());
            case LONG -> formatInteger(statistics.longerTextLength());
            case LEN -> formatInteger(statistics.totalMatchLength());
        };
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String formatInteger(long value) {
        return Long.toString(value);
    }

    private Result storeText(String identifier, Path source, String content) {
        boolean wasPresent = this.loadedTexts.containsKey(identifier);
        this.loadedTexts.put(identifier, new LoadedText(identifier, source, content));

        return Result.success((wasPresent ? MESSAGE_UPDATED : MESSAGE_LOADED).formatted(identifier));
    }

    private record LoadedText(String identifier, Path path, String content) {
    }

    private record PairKey(String firstIdentifier, String secondIdentifier) {
    }

    private static final class PairStatistics {
        private final String firstIdentifier;
        private final String secondIdentifier;
        private final int firstTextLength;
        private final int secondTextLength;
        private long totalMatchLength;
        private int matchCount;
        private int maxMatchLength;
        private int minMatchLength = Integer.MAX_VALUE;

        PairStatistics(String firstIdentifier, String secondIdentifier, int firstTextLength, int secondTextLength) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.firstTextLength = firstTextLength;
            this.secondTextLength = secondTextLength;
        }

        void addMatch(int length) {
            this.totalMatchLength += length;
            this.matchCount++;
            if (length > this.maxMatchLength) {
                this.maxMatchLength = length;
            }
            if (length < this.minMatchLength) {
                this.minMatchLength = length;
            }
        }

        String firstIdentifier() {
            return this.firstIdentifier;
        }

        String secondIdentifier() {
            return this.secondIdentifier;
        }

        double averageLength() {
            if (this.matchCount == 0) {
                return 0.0d;
            }
            return (double) this.totalMatchLength / this.matchCount;
        }

        int maxLength() {
            return this.matchCount == 0 ? 0 : this.maxMatchLength;
        }

        int minLength() {
            return this.matchCount == 0 ? 0 : this.minMatchLength;
        }

        long totalMatchLength() {
            return this.totalMatchLength;
        }

        int longerTextLength() {
            return Math.max(this.firstTextLength, this.secondTextLength);
        }
    }

    private record PairMetricValue(PairStatistics statistics, double value) {
    }
}
