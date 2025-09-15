package edu.kit.kastle.filesorter.view.command;

import edu.kit.kastle.filesorter.view.CommandHandler;
import edu.kit.kastle.filesorter.view.Result;

/**
 * Command that terminates the application.
 */
public class QuitCommand extends Command {

    private static final String COMMAND_NAME = "quit";

    private final CommandHandler commandHandler;

    /**
     * Creates a new quit command.
     *
     * @param commandHandler the command handler managing the application lifecycle
     */
    public QuitCommand(CommandHandler commandHandler) {
        super(COMMAND_NAME, 0);
        this.commandHandler = commandHandler;
    }

    @Override
    protected Result executeCommand(String[] arguments) {
        this.commandHandler.stop();
        return Result.success();
    }
}
