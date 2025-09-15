package edu.kit.kastle.filesorter.view.command;

public abstract class Command {

    private static final String INVALID_ARGUMENTS_ERROR_FORMAT = "%s command takes %d parameters.";

    private final String commandName;
    private final int expectedArgumentLength;

    /**
     * Constructs a new Command.
     *
     * @param commandName            the name of the command
     * @param expectedArgumentLength the number of arguments that the command expects
     */
    protected Command(String commandName, int expectedArgumentLength) {
        this.commandName = commandName;
        this.expectedArgumentLength = expectedArgumentLength;
    }
}
