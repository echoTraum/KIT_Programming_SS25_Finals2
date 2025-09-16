package edu.kit.kastel.filesorter.view;

/**
 * This interface represents a command that can be executed to handle the given value.
 * @param <T> the type of the value to be handled
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
@FunctionalInterface
public interface Command<T> {

    /**
     * Executes the command to handle the given value.
     * @param handle the value to be handled
     * @return the result of the command execution
     */
    Result execute(T handle);
}
