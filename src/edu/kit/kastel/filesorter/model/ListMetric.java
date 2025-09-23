package edu.kit.kastel.filesorter.model;

/**
 * Metrics supported by the list command when summarizing analysis results.
 *
 * @author ugsrv
 */
public enum ListMetric {
    /**
     * Lists the average length of matches between text pairs.
     */
    AVG,

    /**
     * Lists the maximum length of matches between text pairs.
     */
    MAX,

    /**
     * Lists the minimum length of matches between text pairs.
     */
    MIN,

    /**
     * Lists the length of the longer text involved in the pair.
     */
    LONG,

    /**
     * Lists the total length of all matches between text pairs.
     */
    LEN
}

