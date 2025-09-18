package edu.kit.kastel.filesorter.model;

import edu.kit.kastel.filesorter.view.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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

    private final Map<String, LoadedText> loadedTexts = new LinkedHashMap<>();

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

    private Result storeText(String identifier, Path source, String content) {
        boolean wasPresent = this.loadedTexts.containsKey(identifier);
        this.loadedTexts.put(identifier, new LoadedText(identifier, source, content));

        return Result.success((wasPresent ? MESSAGE_UPDATED : MESSAGE_LOADED).formatted(identifier));
    }

    private record LoadedText(String identifier, Path path, String content) {
    }
}
