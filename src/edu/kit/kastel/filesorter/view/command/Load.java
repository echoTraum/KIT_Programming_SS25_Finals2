package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

import java.nio.file.Path;

/**
 * Command that loads a text from the provided file path into the {@link SequenceMatcher}.
 */
public class Load implements Command<SequenceMatcher> {

    private final Path path;

    /**
     * Creates a new command.
     *
     * @param path the path to load the text from
     */
    public Load(Path path) {
        this.path = path;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.load(this.path);
    }
}
