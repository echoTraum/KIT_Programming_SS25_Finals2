package edu.kit.kastel.filesorter.view;

/**
 * Represents an interactive controller that handles commands in a specialised editing mode.
 */
public interface EditingController {

    /**
     * Called once when the editing mode is entered. Implementations should output the initial
     * status information of the mode.
     */
    void onEnter();

    /**
     * Handles a single command entered while the editing mode is active.
     *
     * @param command the command tokens as provided by the user
     * @return {@code true} if the command was recognised (irrespective of whether it succeeded),
     *         {@code false} otherwise
     */
    boolean handleCommand(String[] command);
}

