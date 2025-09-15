package edu.kit.kastle.filesorter.view;

import edu.kit.kastle.filesorter.view.command.Command;
import edu.kit.kastle.filesorter.view.command.QuitCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class handles the user input and executes the commands.
 *
 * @author ugsrv
 */
public class CommandHandler {
    private static final String ERROR_PREFIX = "ERROR: ";
    private static final String ERROR_COMMAND_NOT_FOUND_FORMAT = "Command '%s' not found";

    private final Map<String, Command> commands = new HashMap<>();
    private final BufferedReader reader;
    private final PrintStream output;
    private boolean running;

    /**
     * Creates a command handler that reads from {@link System#in} and writes to {@link System#out}.
     */
    public CommandHandler() {
        this(System.in, System.out);
    }

    /**
     * Creates a command handler with the given input and output streams.
     *
     * @param input  the input stream to read commands from
     * @param output the output stream to write results to
     */
    public CommandHandler(InputStream input, PrintStream output) {
        Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.running = true;
        this.initializeCommands();
    }

    private void initializeCommands() {
        addCommand(new QuitCommand(this));
    }

    private void addCommand(Command command) {
        Command nonNullCommand = Objects.requireNonNull(command, "command");
        this.commands.put(nonNullCommand.getCommandName(), nonNullCommand);
    }

    /**
     * Starts processing user input until the application is instructed to quit or the input stream ends.
     */
    public void run() {
        while (this.running) {
            String line = this.readLine();
            if (line == null) {
                break;
            }
            if (line.isBlank()) {
                continue;
            }
            Result result = this.handleCommand(line);
            this.printResult(result);
        }
    }

    private String readLine() {
        try {
            return this.reader.readLine();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read from input", exception);
        }
    }

    /**
     * Handles a single command line.
     *
     * @param input the user input
     * @return the result of the command execution
     */
    public Result handleCommand(String input) {
        Objects.requireNonNull(input, "input");
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return Result.success();
        }
        String[] tokens = trimmed.split("\\s+");
        String commandName = tokens[0];
        Command command = this.commands.get(commandName);
        if (command == null) {
            return Result.error(String.format(ERROR_COMMAND_NOT_FOUND_FORMAT, commandName));
        }
        String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);
        return command.execute(arguments);
    }

    private void printResult(Result result) {
        if (result == null) {
            return;
        }
        String message = result.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        if (result.getType() == ResultType.FAILURE) {
            this.output.println(ERROR_PREFIX + message);
        } else {
            this.output.println(message);
        }
        this.output.flush();
    }

    /**
     * Stops the command handler. Subsequent calls to {@link #run()} will exit the processing loop.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Returns whether the handler is still processing commands.
     *
     * @return {@code true} if the handler is processing commands; {@code false} otherwise
     */
    public boolean isRunning() {
        return this.running;
    }
}
