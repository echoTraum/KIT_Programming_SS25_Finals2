package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.model.TokenizationStrategy;
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
    LOAD(arguments -> new Load(parsePath(arguments))),

    /**
     * Keyword for the {@link Clear} command.
     */
    CLEAR(arguments -> new Clear()),

    /**
     * Keyword for the {@link Tokenization} command.
     */
    TOKENIZATION(arguments -> new Tokenization(arguments.parseString(), parseTokenizationStrategy(arguments))),

    /**
     * Keyword for the {@link Analyze} command.
     */
    ANALYZE(arguments -> new Analyze(parseTokenizationStrategy(arguments), arguments.parsePositive())),

    /**
     * Keyword for the {@link Matches} command.
     */
    MATCHES(arguments -> new Matches(arguments.parseString(), arguments.parseString())),

    /**
     * Keyword for the {@link Edit} command.
     */
    EDIT(arguments -> new Edit(arguments.parseString(), arguments.parseString())),

    /**
     * Keyword for the {@link List} command.
     */
    LIST(arguments -> new List(parseListMetric(arguments), parseSortOrder(arguments))),

    /**
     * Keyword for the {@link Top} command.
     */
    TOP(arguments -> new Top(arguments.parsePositive(), parseListMetric(arguments),
            parseSortOrder(arguments))),

    /**
     * Keyword for the {@link Histogram} command.
     */
    HISTOGRAM(arguments -> new Histogram(parseListMetric(arguments)));

    private static final String ERROR_INVALID_PATH = "invalid path";
    private static final String ERROR_INVALID_STRATEGY = "invalid strategy";
    private static final String ERROR_INVALID_METRIC = "invalid metric";
    private static final String ERROR_INVALID_ORDER = "invalid order";
    private static final String VALUE_NAME_DELIMITER = "_";
    private final CommandProvider<SequenceMatcher> provider;


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

    private static TokenizationStrategy parseTokenizationStrategy(Arguments arguments)
            throws InvalidArgumentException {
        String strategyArgument = arguments.parseString();
        TokenizationStrategy strategy = TokenizationStrategy.findByName(strategyArgument);
        if (strategy == null) {
            throw new InvalidArgumentException(ERROR_INVALID_STRATEGY);
        }
        return strategy;
    }

    private static ListMetric parseListMetric(Arguments arguments) throws InvalidArgumentException {
        String metricArgument = arguments.parseString();
        ListMetric metric = ListMetric.fromString(metricArgument);
        if (metric == null) {
            throw new InvalidArgumentException(ERROR_INVALID_METRIC);
        }
        return metric;
    }

    private static SortOrder parseSortOrder(Arguments arguments)
            throws InvalidArgumentException {
        if (arguments.isExhausted()) {
            return SortOrder.DESCENDING;
        }
        String orderArgument = arguments.parseString();
        SortOrder order = SortOrder.fromString(orderArgument);
        if (order == null) {
            throw new InvalidArgumentException(ERROR_INVALID_ORDER);
        }
        return order;
    }
}
