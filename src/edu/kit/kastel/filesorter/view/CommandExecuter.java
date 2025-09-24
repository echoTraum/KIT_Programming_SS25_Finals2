package edu.kit.kastel.filesorter.view;


import edu.kit.kastel.filesorter.view.command.ViewKeyword;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collection;

/**
 * <p> The class initiates and handles command executions with the user. It is responsible for delegating the input to the 
 * corresponding command implementations. The command and its arguments are expected to be separated by {@value #COMMAND_SEPARATOR}.</p>
 *
 * <p> An interaction is started by calling {@link #handleUserInput()}. It is possible to stop the current interaction prematurely 
 * with {@link #stop()}.</p>
 *
 * <p> As example, if the interaction should happen via the command line, the standard input/output streams should be provided.</p>
 * 
 * @param <M> the type of the model on which model commands are invoked
 * @param <K> the type of the model commands provider
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public class CommandExecuter<M, K extends Enum<K> & Keyword<M>> {

    private static final String COMMAND_SEPARATOR = " ";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String ERROR_UNKNOWN_COMMAND = ERROR_PREFIX + "unknown command";
    private static final String ERROR_TOO_MANY_ARGUMENTS = ERROR_PREFIX + "too many arguments provided.";
    private static final String ERROR_INVALID_PRECONDITION = ERROR_PREFIX + "command cannot be used right now.";
    private final Set<? extends Keyword<M>> modelKeywords;
    private final Set<ViewKeyword> viewKeywords = EnumSet.allOf(ViewKeyword.class);
    private final Scanner scanner;
    private final PrintStream defaultStream;
    private final PrintStream errorStream;
    private M model;
    private boolean isRunning;
    private EditingController editingController;

    /**
     * Constructs a new command executer using the provided input source and output streams when interacting.
     *
     * @param inputSource the input source used to retrieve the user input
     * @param defaultOutputStream the stream used to print the default output
     * @param errorStream the stream used to print the error output
     * @param keywordClass the class of the command provider to look up possible commands
     */
    public CommandExecuter(Scanner inputSource, PrintStream defaultOutputStream, PrintStream errorStream, Class<K> keywordClass) {
        this.scanner = inputSource;
        this.defaultStream = defaultOutputStream;
        this.errorStream = errorStream;
        this.modelKeywords = EnumSet.allOf(keywordClass);
        this.isRunning = true;
    }

    /**
     * Returns the default stream that this executer has been registered with.
     *
     * @return the default stream of this executer
     */
    public PrintStream getDefaultStream() {
        return this.defaultStream;
    }

    /**
     * Returns the error stream that this executer uses for reporting problems.
     *
     * @return the error stream of this executer
     */
    public PrintStream getErrorStream() {
        return this.errorStream;
    }

    /**
     * Sets the model of this command executer on which its commands should get invoked.
     * 
     * @param model the model to be handled by the commands
     */
    public void setModel(M model) {
        this.model = model;
    }

    /**
     * Returns whether this executer has been terminated.
     *
     * @return whether this executer has been terminated
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Stops this instance from reading further input from the source.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Retrieves a command from the provided input stream and executes it. This method will block while waiting for an input.
     *
     * @see Scanner#hasNextLine()
     */
    public void handleUserInput() {
        if (this.isRunning && this.scanner.hasNextLine()) {
            handleLine(this.scanner.nextLine());
        }
    }

    private void handleLine(String line) {
        String[] splittedLine = line.split(COMMAND_SEPARATOR, -1);
        if (this.editingController != null) {
            if (!findAndHandleCommand(this.viewKeywords, this, splittedLine)) {
                return;
            }
            if (!this.editingController.handleCommand(splittedLine)) {
                this.errorStream.println(ERROR_UNKNOWN_COMMAND);
            }
            return;
        }
        if (findAndHandleCommand(this.viewKeywords, this, splittedLine)
                && findAndHandleCommand(this.modelKeywords, this.model, splittedLine)) {
            this.errorStream.println(ERROR_UNKNOWN_COMMAND);
        }
    }

    private <S, T extends Keyword<S>> boolean findAndHandleCommand(Set<T> keywords, S value, String[] command) {
        T keyword = retrieveKeyword(keywords, command);
        if (keyword != null) {
            String[] arguments = Arrays.copyOfRange(command, keyword.words(), command.length);
            handleCommand(value, arguments, keyword);
            return false;
        }
        return true;
    }

    private <S, T extends Keyword<S>> void handleCommand(S value, String[] arguments, T keyword) {
        if (value == null) {
            this.errorStream.println(ERROR_INVALID_PRECONDITION);
            return;
        }

        Arguments argumentsHolder = new Arguments(arguments);
        Command<S> providedCommand;
        try {
            providedCommand = keyword.provide(argumentsHolder);
        } catch (InvalidArgumentException e) {
            this.errorStream.println(ERROR_PREFIX + e.getMessage());
            return;
        }

        if (!argumentsHolder.isExhausted()) {
            this.errorStream.println(ERROR_TOO_MANY_ARGUMENTS);
            return;
        }

        Result result;
        if (providedCommand instanceof ExecuterCommand<?> executerCommand) {
            ExecuterCommand<S> typedCommand = (ExecuterCommand<S>) executerCommand;
            result = typedCommand.execute(value, this);
        } else {
            result = providedCommand.execute(value);
        }
        handleResult(result);
    }

    private void handleResult(Result result) {
        if (result == null || result.getMessage() == null) {
            return;
        }

        PrintStream outputStream = switch (result.getType()) {
            case SUCCESS -> this.defaultStream;
            case FAILURE -> this.errorStream;
        };
        outputStream.println((result.getType().equals(ResultType.FAILURE) ? ERROR_PREFIX : "") + result.getMessage());
    }

    private static <T extends Keyword<?>> T retrieveKeyword(Collection<T> keywords, String[] command) {
        for (T keyword : keywords) {
            if (keyword.matches(command)) {
                return keyword;
            }
        }
        return null;
    }

    /**
     * Activates a specialised editing mode handled by the provided controller.
     *
     * @param controller the controller managing the editing interaction
     */
    public void enterEditingMode(EditingController controller) {
        this.editingController = controller;
        controller.onEnter();
    }

    /**
     * Leaves the currently active editing mode, if any.
     */
    public void exitEditingMode() {
        this.editingController = null;
    }
}
