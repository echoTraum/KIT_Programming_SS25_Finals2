package edu.kit.kastel.filesorter.view.command;

import java.util.Locale;
import java.util.Objects;

/**
 * Enumeration describing the possible sort orders for list based commands.
 */
public enum SortOrder {
    /** Sorts results in ascending order. */
    ASCENDING,
    /** Sorts results in descending order. */
    DESCENDING;

    /**
     * Parses the provided textual representation into a {@link SortOrder}.
     *
     * @param value the textual representation
     * @return the corresponding sort order or {@code null} if the value could not be parsed
     */
    public static SortOrder fromString(String value) {
        Objects.requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ASC", "ASCENDING" -> ASCENDING;
            case "DESC", "DESCENDING" -> DESCENDING;
            default -> null;
        };
    }
}
