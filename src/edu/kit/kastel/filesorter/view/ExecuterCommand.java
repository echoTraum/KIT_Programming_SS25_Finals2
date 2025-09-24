package edu.kit.kastel.filesorter.view;

/**
 * Extension of {@link Command} that requires access to the surrounding {@link CommandExecuter}.
 *
 * <p>This interface is used by commands that need to modify the behaviour of the executer itself,
 * for example by entering a specialised interaction mode. Implementations should override the
 * method accepting the executer. The default {@link #execute(Object)} implementation throws an
 * {@link UnsupportedOperationException} to ensure the correct overload is used.</p>
 *
 * @param <T> the type of the model handled by the command
 */
public interface ExecuterCommand<T> extends Command<T> {

    /**
     * Executes the command using the provided handle and executer.
     *
     * @param handle the value to operate on
     * @param executer the executer invoking the command
     * @return the result of the execution
     */
    Result execute(T handle, CommandExecuter<?, ?> executer);

    @Override
    default Result execute(T handle) {
        throw new UnsupportedOperationException("ExecuterCommand must be executed with the executer instance.");
    }
}

