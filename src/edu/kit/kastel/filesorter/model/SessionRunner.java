package edu.kit.kastel.filesorter.model;

/**
 * Provides the functionality for game clients.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public interface SessionRunner {
    /**
     * Creates the SequenceMatcher.
     * @return the SequenceMatcher to be created.
     */
    SequenceMatcher createSequenceMatcher();

    /**
     * Executes a players action on a given game instance.
     * @param handle the game in question.
     */
    void executeAction(SequenceMatcher handle);

    /**
     * Registers if the game was ended forcefully or not.
     * @return true if it was ended, false if not.
     */
    boolean wasQuit();
}