package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.ComparisonEditor;
import edu.kit.kastel.filesorter.model.ComparisonEditingException;
import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.CommandExecuter;
import edu.kit.kastel.filesorter.view.ExecuterCommand;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that enters the interactive editing mode for a pair of texts.
 */
public class Edit implements ExecuterCommand<SequenceMatcher> {

    private final String firstIdentifier;
    private final String secondIdentifier;

    /**
     * Creates a new edit command for the given identifiers.
     *
     * @param firstIdentifier the identifier of the first text
     * @param secondIdentifier the identifier of the second text
     */
    public Edit(String firstIdentifier, String secondIdentifier) {
        this.firstIdentifier = firstIdentifier;
        this.secondIdentifier = secondIdentifier;
    }

    @Override
    public Result execute(SequenceMatcher handle, CommandExecuter<?, ?> executer) {
        try {
            ComparisonEditor editor = handle.openEditor(this.firstIdentifier, this.secondIdentifier);
            ComparisonEditingController controller = new ComparisonEditingController(executer, editor,
                    executer.getDefaultStream(), executer.getErrorStream());
            executer.enterEditingMode(controller);
            return Result.success();
        } catch (ComparisonEditingException e) {
            return Result.error(e.getMessage());
        }
    }
}

