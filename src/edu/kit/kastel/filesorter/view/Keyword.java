/*
 * Copyright (c) 2025, KASTEL. All rights reserved.
 */

package edu.kit.kastel.filesorter.view;

/**
 * This interface represents a keyword that can be used to identify a command.
 * @param <T> the type of the value that is handled by the command
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public interface Keyword<T> extends CommandProvider<T> {

    /**
     * Returns whether the keyword matches the given command.
     * @param command the command to be checked
     * @return whether the keyword matches the command
     */
    boolean matches(String[] command);

    /**
     * Returns the number of words.
     * @return the number of words
     */
    int words();
}
