package edu.kit.kastel.filesorter.view;

import java.util.Arrays;

/**
 * This class represents the arguments of a {@link Command}.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public class Arguments {

    private static final String ERROR_TOO_FEW_ARGUMENTS = "too few arguments";
    private static final String ERROR_NOT_A_NUMBER_FORMAT = "'%s' must be an integer.";
    private static final String ERROR_NOT_POSITIVE_FORMAT = "'%d' must be positive.";
    private final String[] arguments;
    private int argumentIndex;

    /**
     * Constructs a new instance.
     *
     * @param arguments the arguments to parse
     */
    public Arguments(String[] arguments) {
        this.arguments = arguments.clone();
    }

    /**
     * Returns whether all provided arguments have been consumed.
     *
     * @return {@code true} if all arguments have been consumed, {@code false} otherwise
     */
    public boolean isExhausted() {
        return argumentIndex >= arguments.length;
    }

    private String retrieveArgument() throws InvalidArgumentException {
        if (isExhausted()) {
            throw new InvalidArgumentException(ERROR_TOO_FEW_ARGUMENTS);
        }
        return arguments[argumentIndex++];
    }

    /**
     * Parses the next argument as a string.
     *
     * @return the argument as a string
     * @throws InvalidArgumentException if there is no argument to parse
     */
    public String parseString() throws InvalidArgumentException {
        return retrieveArgument();
    }

    /**
     * Parses the remaining arguments as a single string separated by spaces.
     *
     * @return the remaining arguments joined by spaces
     * @throws InvalidArgumentException if there is no argument left to parse
     */
    public String parseRemaining() throws InvalidArgumentException {
        if (isExhausted()) {
            throw new InvalidArgumentException(ERROR_TOO_FEW_ARGUMENTS);
        }

        String remaining = String.join(" ", Arrays.copyOfRange(arguments, argumentIndex, arguments.length));
        argumentIndex = arguments.length;
        return remaining;
    }

    /**
     * Parses the next argument as an integer.
     *
     * @return the argument as an integer
     * @throws InvalidArgumentException if the argument could not get parsed
     */
    public int parseInteger() throws InvalidArgumentException {
        String argument = retrieveArgument();
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException(ERROR_NOT_A_NUMBER_FORMAT.formatted(argument));
        }
    }

    /**
     * Parses the next argument as a positive integer.
     *
     * @return the next argument as a positive integer
     * @throws InvalidArgumentException if the argument could not get parsed into a positive integer
     */
    public int parsePositive() throws InvalidArgumentException {
        int value = parseInteger();
        if (value < 1) {
            throw new InvalidArgumentException(ERROR_NOT_POSITIVE_FORMAT.formatted(value));
        }
        return value;
    }
}
