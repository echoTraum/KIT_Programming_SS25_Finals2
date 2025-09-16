package edu.kit.kastel.filesorter;

import edu.kit.kastel.filesorter.model.SequenceMatcherRunner;
import edu.kit.kastel.filesorter.view.CLISessionRunner;

/**
 * The class offering the entry point for the application.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public final class Application {

    private static final String ERROR_MESSAGE_COMMAND_LINE_ARGUMENTS = "Error: Expected no command line arguments";

    private Application() {
        // utility class
    }

    /**
     * The entry point for the application. No command line arguments are expected.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 0) {
            System.err.println(ERROR_MESSAGE_COMMAND_LINE_ARGUMENTS);
            return;
        }
        try (CLISessionRunner sessionRunner = new CLISessionRunner(System.in, System.out, System.err)) {
            SequenceMatcherRunner sequenceRunner = new SequenceMatcherRunner(sessionRunner);
            sequenceRunner.start();

        }

    }
}