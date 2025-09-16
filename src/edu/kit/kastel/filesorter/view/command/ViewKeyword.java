package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.CommandExecuter;
import edu.kit.kastel.filesorter.view.CommandProvider;
import edu.kit.kastel.filesorter.view.Keyword;
import edu.kit.kastel.filesorter.view.Arguments;
import edu.kit.kastel.filesorter.view.InvalidArgumentException;

/**
 * This enum represents all keywords for commands handling an {@link CommandExecuter}.
 * 
 * @author Programmieren-Team
 */
public enum ViewKeyword implements Keyword<CommandExecuter<?, ?>> {

    /**
     * The keyword for the {@link Quit quit} command.
     */
    QUIT(arguments -> new Quit());

    private static final String VALUE_NAME_DELIMITER = "_";
    private final CommandProvider<CommandExecuter<?, ?>> provider;

    ViewKeyword(CommandProvider<CommandExecuter<?, ?>> provider) {
        this.provider = provider;
    }
    
    @Override
    public Command<CommandExecuter<?, ?>> provide(Arguments arguments) throws InvalidArgumentException {
        return provider.provide(arguments);
    }

    @Override
    public boolean matches(String[] command) {
        return name().toLowerCase().equals(command[0]);
    }

    @Override
    public int words() {
        return name().split(VALUE_NAME_DELIMITER).length;
    }
}
