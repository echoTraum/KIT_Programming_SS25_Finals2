package edu.kit.kastel.filesorter.view.command;

import java.util.OptionalInt;

/**
 * Command that lists only the top program pairs of a list invocation.
 */
public class Top extends AbstractPairListCommand {

    /**
     * Creates a new command instance.
     *
     * @param limit the maximum number of program pairs to list
     * @param metric the metric to use for ordering
     * @param order the order in which the results should be listed
     */
    public Top(int limit, ListMetric metric, SortOrder order) {
        super(metric, order, OptionalInt.of(limit));
    }
}
