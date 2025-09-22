package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that removes all texts currently stored in the {@link SequenceMatcher}.
 */
public class Clear implements Command<SequenceMatcher> {

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.clear();
    }
}
