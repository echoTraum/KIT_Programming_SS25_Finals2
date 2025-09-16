package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.CommandExecuter;
import edu.kit.kastel.filesorter.view.Result;

/**
 * This class represents a command that quits an interaction of an user interface. No arguments are expected.
 * 
 * @author Programmieren-Team
 */
public class Quit implements Command<CommandExecuter<?, ?>> {

    /**
     * Quits an interaction of the provided user interface by invoking {@link CommandExecuter#stop()}.
     * 
     * @param handle the user interface to be stopped
     * @return {@code null}
     */
    @Override
    public Result execute(CommandExecuter<?, ?> handle) {
        handle.stop();
        return null;
    }
}
