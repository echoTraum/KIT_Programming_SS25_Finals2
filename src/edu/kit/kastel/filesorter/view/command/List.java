package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.ListMetric;
import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.model.SortOrder;
import edu.kit.kastel.filesorter.view.Arguments;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.InvalidArgumentException;
import edu.kit.kastel.filesorter.view.Result;

import java.util.Locale;

/**
 * Command that lists aggregated match statistics for each analyzed text pair.
 */
public class List implements Command<SequenceMatcher> {

    private static final String ERROR_INVALID_METRIC = "invalid metric";
    private static final String ERROR_INVALID_ORDER = "invalid order";

    private final ListMetric metric;
    private final SortOrder order;

    /**
     * Creates a new list command.
     *
     * @param metric the metric to display
     * @param order the sort order to apply
     */
    public List(ListMetric metric, SortOrder order) {
        this.metric = metric;
        this.order = order;
    }

    /**
     * Parses the command arguments and constructs a new {@link List} command instance.
     *
     * @param arguments the arguments to parse
     * @return the created command instance
     * @throws InvalidArgumentException if the provided arguments are invalid
     */
    public static List fromArguments(Arguments arguments) throws InvalidArgumentException {
        String metricArgument = arguments.parseString();
        ListMetric metric = parseMetric(metricArgument);

        SortOrder order = SortOrder.DESC;
        if (!arguments.isExhausted()) {
            String orderArgument = arguments.parseString();
            order = parseOrder(orderArgument);
        }

        return new List(metric, order);
    }

    private static ListMetric parseMetric(String value) throws InvalidArgumentException {
        try {
            return ListMetric.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(ERROR_INVALID_METRIC);
        }
    }

    private static SortOrder parseOrder(String value) throws InvalidArgumentException {
        try {
            return SortOrder.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(ERROR_INVALID_ORDER);
        }
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.list(this.metric, this.order);
    }
}

