package edu.kit.kastel.filesorter.view.command;

import java.util.OptionalInt;

/**
 * Command that lists all program pairs ordered by a configurable metric.
 *
 * @author ugsrv
 */
public class List extends AbstractPairListCommand {

    /**
     * Creates a new command instance.
     *
     * @param metric the metric to use for ordering
     * @param order the order in which the results should be listed
     */
    public List(ListMetric metric, SortOrder order) {
        super(metric, order, OptionalInt.empty());
    }

}
