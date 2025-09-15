package edu.kit.kastle.filesorter.view;

/**
 * This class handles the user input and executes the commands.
 *
 * @author ugsrv
 */
public class CommandHandler {
    private static final String ERROR_PREFIX = "ERROR: ";
    private static final String ERROR_COMMAND_NOT_FOUND_FORMAT = "Command '%s' not found";

    private CommandHandler() {
        this.initializeCommands();
    }

    private void initializeCommands() {
        addCommand(new QuitCommand(this));
    }

    private void addCommand(Command command) {
        this.commands.add(command);
    }
}
