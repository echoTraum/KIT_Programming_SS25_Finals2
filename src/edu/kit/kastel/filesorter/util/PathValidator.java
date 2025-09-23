package edu.kit.kastel.filesorter.util;

import java.util.Locale;

/**
 * Utility methods for validating file path strings.
 *
 * <p>The validation aims to prevent {@link java.nio.file.InvalidPathException}
 * by rejecting strings that are known to be invalid on common platforms.</p>
 *
 * @author Programmieren-Team
 */
public final class PathValidator {

    private static final char NULL_CHARACTER = '\0';
    private static final String WINDOWS_IDENTIFIER = "windows";
    private static final char WINDOWS_DRIVE_SEPARATOR = ':';
    private static final char[] WINDOWS_INVALID_CHARACTERS = {'<', '>', '"', '|', '?', '*'};
    private static final boolean IS_WINDOWS = System.getProperty("os.name")
            .toLowerCase(Locale.ROOT)
            .contains(WINDOWS_IDENTIFIER);

    private PathValidator() {
    }

    /**
     * Determines whether the provided path string can be safely used on the current platform.
     *
     * @param path the path string to validate
     * @return {@code true} if the string does not contain invalid characters, {@code false} otherwise
     */
    public static boolean isValid(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf(NULL_CHARACTER) >= 0) {
            return false;
        }
        return !IS_WINDOWS || isValidOnWindows(path);
    }

    private static boolean isValidOnWindows(String path) {
        for (char character : WINDOWS_INVALID_CHARACTERS) {
            if (path.indexOf(character) >= 0) {
                return false;
            }
        }
        int colonIndex = path.indexOf(WINDOWS_DRIVE_SEPARATOR);
        if (colonIndex == -1) {
            return true;
        }
        if (path.startsWith("\\\\?\\")) {
            int driveLetterIndex = colonIndex - 1;
            return driveLetterIndex > 3
                    && Character.isLetter(path.charAt(driveLetterIndex))
                    && path.indexOf(WINDOWS_DRIVE_SEPARATOR, colonIndex + 1) == -1;
        }
        if (colonIndex != 1 || !Character.isLetter(path.charAt(0))) {
            return false;
        }
        return path.indexOf(WINDOWS_DRIVE_SEPARATOR, colonIndex + 1) == -1;
    }
}
