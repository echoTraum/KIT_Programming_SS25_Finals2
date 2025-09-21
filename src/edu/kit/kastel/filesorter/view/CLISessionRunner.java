package edu.kit.kastel.filesorter.view;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.model.SessionRunner;
import edu.kit.kastel.filesorter.view.command.ModelKeyword;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * A command-line interface (CLI) implementation of a GameClient. This class enables interaction with
 * the game via the command line by reading user input from an input stream and providing responses
 * via output streams.
 * <p>
 * It uses a {@link CommandExecuter} to handle and execute commands related to the game model.
 * Commands are parsed and handled based on predefined keywords.
 * <p>
 * Instances of CLISessionRunner should be properly closed after use to ensure associated resources are
 * released.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public class CLISessionRunner implements SessionRunner, AutoCloseable {
    private static final String WELCOME_MESSAGE = "Use one of the following commands : "
            + "load , input , tokenization , analyze , clear , list , top , matches , histogram , edit , inspect , quit.";

    private final Scanner scanner;
    private final CommandExecuter<SequenceMatcher, ModelKeyword> executer;
    private boolean wasQuit = false;

    /**
     * Constructs a new client using the provided input source and output streams when interacting.
     *
     * @param inputSource the input source used to retrieve the user input
     * @param defaultOutputStream the stream used to print the default output
     * @param errorStream the stream used to print the error output
     */
    public CLISessionRunner(InputStream inputSource, PrintStream defaultOutputStream, PrintStream errorStream) {
        this.scanner = new Scanner(inputSource);
        this.executer = new CommandExecuter<>(this.scanner, defaultOutputStream, errorStream, ModelKeyword.class);
    }

    @Override
    public SequenceMatcher createSequenceMatcher() {
        this.executer.getDefaultStream().println(WELCOME_MESSAGE);
        SequenceMatcher handle = new SequenceMatcher();
        this.executer.setModel(handle);
        return handle;
    }

    @Override
    public void executeAction(SequenceMatcher handle) {
        this.executer.setModel(handle);
        this.executer.handleUserInput();
        this.wasQuit = !this.executer.isRunning();
    }

    @Override
    public boolean wasQuit() {
        return this.wasQuit;
    }

    @Override
    public void close() {
        this.scanner.close();
    }
}