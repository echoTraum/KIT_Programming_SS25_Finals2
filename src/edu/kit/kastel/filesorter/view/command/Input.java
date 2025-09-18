package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that stores a text provided via the command line directly in the {@link SequenceMatcher}.
 */
public class Input implements Command<SequenceMatcher> {

    private final String identifier;
    private final String text;

    /**
     * Creates a new command.
     *
     * @param identifier the identifier to store the text under
     * @param text the text to store
     */
    public Input(String identifier, String text) {
        this.identifier = identifier;
        this.text = text;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.input(this.identifier, this.text);
    }
}
