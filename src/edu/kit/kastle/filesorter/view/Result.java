package edu.kit.kastle.filesorter.view;

/**
 * The result of a command execution.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public final class Result {
    
    private final ResultType type;
    private final String message;

    private Result(ResultType type, String message) {
        this.type = type;
        this.message = message;
    }

    /**
     * Returns the type of the result.
     * @return the type of the result
     */
    public ResultType getType() {
        return type;
    }

    /**
     * Returns the message of the result.
     * @return the message of the result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Creates a new error result with the given message. The {@link #getType()} method will return {@link ResultType#FAILURE}.
     * @param message the message of the result
     * @return a new error result
     */
    public static Result error(String message) {
        return new Result(ResultType.FAILURE, message);
    }

    /**
     * Creates a new success result without any message. The {@link #getType()} method will return {@link ResultType#SUCCESS},
     * the {@link #getMessage()} method will return {@code null}.
     * @return a new success result without any message
     */
    public static Result success() {
        return new Result(ResultType.SUCCESS, null);
    }

    /**
     * Creates a new success result with the given message. The {@link #getType()} method will return {@link ResultType#SUCCESS}.
     * @param message the message of the result
     * @return a new success result
     */
    public static Result success(String message) {
        return new Result(ResultType.SUCCESS, message);
    }
}
