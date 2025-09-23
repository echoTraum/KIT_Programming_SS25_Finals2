package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that loads a text from the provided file path into the {@link SequenceMatcher}.
 *
 * @author ugsrv
 */
public class Load implements Command<SequenceMatcher> {

    private final String path;

    /**
     * Creates a new command.
     *
     * @param path the path to load the text from
     */
    public Load(String path) {
        this.path = path;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.load(this.path);
    }
}
