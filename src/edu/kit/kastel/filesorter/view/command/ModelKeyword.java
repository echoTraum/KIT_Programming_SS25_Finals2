package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;

import edu.kit.kastel.filesorter.view.Arguments;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.CommandProvider;
import edu.kit.kastel.filesorter.view.InvalidArgumentException;
import edu.kit.kastel.filesorter.view.Keyword;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * This enum represents all keywords for commands handling a {@link SequenceMatcher}.
 *
 * @author ugsrv
 * @author Programmieren-Team
 */
public enum ModelKeyword implements Keyword<SequenceMatcher> {
    /**
     * Keyword for the {@link Input} command.
     */
    INPUT(arguments -> new Input(arguments.parseString(), parseText(arguments))),

    /**
     * Keyword for the {@link Load} command.
     */
    LOAD(arguments -> new Load(parsePath(arguments)));


    private static final String VALUE_NAME_DELIMITER = "_";
    private final CommandProvider<SequenceMatcher> provider;
    private static final String ERROR_INVALID_PATH = "invalid path";

    ModelKeyword(CommandProvider<SequenceMatcher> provider) {
        this.provider = provider;
    }

    @Override
    public Command<SequenceMatcher> provide(Arguments arguments) throws InvalidArgumentException {
        return this.provider.provide(arguments);
    }

    @Override
    public boolean matches(String[] command) {
        String[] splittedKeyword = name().split(VALUE_NAME_DELIMITER);
        if (command.length < splittedKeyword.length) {
            return false;
        }
        for (int i = 0; i < splittedKeyword.length; i++) {
            if (!splittedKeyword[i].toLowerCase().equals(command[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int words() {
        return name().split(VALUE_NAME_DELIMITER).length;
    }

    private static Path parsePath(Arguments arguments) throws InvalidArgumentException {
        String pathArgument = arguments.parseString();
        try {
            return Path.of(pathArgument);
        } catch (InvalidPathException e) {
            throw new InvalidArgumentException(ERROR_INVALID_PATH);
        }
    }

    private static String parseText(Arguments arguments) throws InvalidArgumentException {
        return arguments.parseRemaining();
    }
}
