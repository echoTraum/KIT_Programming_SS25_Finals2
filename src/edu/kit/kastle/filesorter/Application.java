package edu.kit.kastle.filesorter;

/**
 * The class offering the entry point for the application.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public final class Application {

    private static final String ERROR_MESSAGE_COMMAND_LINE_ARGUMENTS = "Error: Expected no command line arguments";
    private static final String WELCOME_MESSAGE = "Use one of the following commands :"
                    + " load , input , tokenization , analyze , clear , list , top , matches , histogram , edit , inspect , quit .";

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
        System.out.println(WELCOME_MESSAGE);
    }
}