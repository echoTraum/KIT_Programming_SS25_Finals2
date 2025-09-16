package edu.kit.kastel.filesorter.model;

/**
 * Represents a runner responsible for executing operations related to a game.
 * The responsibility of this class lies in encapsulating the {@link SessionRunner}
 * to manage and execute game-related tasks.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */

public class SequenceMatcherRunner {
    private final SessionRunner client;

    /**
     * Creates a new runner for a given client.
     *
     * @param client the client for the game.
     */
    public SequenceMatcherRunner(SessionRunner client) {
        this.client = client;
    }

    /**
     * Starts the game execution process using the associated {@link SessionRunner}.
     * This method initializes a new game instance and repeatedly processes actions
     * until the client signals that the game has been quit.
     * <p>
     * The process involves the following steps:
     * 1. A new {@link SequenceMatcher} instance is created through the client.
     * If no game is created, the method terminates.
     * 2. Actions are subsequently executed on the game instance in a loop until the client confirms
     *    that the game has been forcefully or voluntarily ended.
     * <p>
     * This method ensures the interaction between the game logic and the client, continuously
     * processing until the quitting condition is met.
     */
    public void start() {
        SequenceMatcher game = this.client.createSequenceMatcher();
        if (game == null) {
            return;
        }
        while (!this.client.wasQuit()) {
            this.client.executeAction(game);
        }
    }
}
