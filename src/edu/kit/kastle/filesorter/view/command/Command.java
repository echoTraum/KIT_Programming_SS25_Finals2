package edu.kit.kastle.filesorter.view.command;

import edu.kit.kastle.filesorter.view.Result;
import java.util.Objects;

/**
 * Represents a user command.
 */
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
        this.commandName = Objects.requireNonNull(commandName, "commandName");
        this.expectedArgumentLength = expectedArgumentLength;
    }

    /**
     * Returns the name of the command.
     *
     * @return the command name
     */
    public String getCommandName() {
        return this.commandName;
    }

    /**
     * Executes the command with the given arguments.
     *
     * @param arguments the arguments supplied by the user
     * @return the result of the command execution
     */
    public final Result execute(String[] arguments) {
        Objects.requireNonNull(arguments, "arguments");
        if (arguments.length != this.expectedArgumentLength) {
            return Result.error(String.format(INVALID_ARGUMENTS_ERROR_FORMAT, this.commandName, this.expectedArgumentLength));
        }
        return this.executeCommand(arguments);
    }

    /**
     * Executes the command. Implementations can assume that the number of arguments matches
     * {@link #expectedArgumentLength}.
     *
     * @param arguments the arguments passed to the command
     * @return the result of the command execution
     */
    protected abstract Result executeCommand(String[] arguments);
}
