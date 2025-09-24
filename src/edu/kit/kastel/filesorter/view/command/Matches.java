package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that displays all matches of the last analysis for a given text pair.
 *
 * @author Programmieren-Team
 */
public class Matches implements Command<SequenceMatcher> {

    private final String firstIdentifier;
    private final String secondIdentifier;

    /**
     * Creates a new matches command.
     *
     * @param firstIdentifier the first identifier
     * @param secondIdentifier the second identifier
     */
    public Matches(String firstIdentifier, String secondIdentifier) {
        this.firstIdentifier = firstIdentifier;
        this.secondIdentifier = secondIdentifier;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.matches(this.firstIdentifier, this.secondIdentifier);
    }
}
