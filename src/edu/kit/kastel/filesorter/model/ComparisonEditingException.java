package edu.kit.kastel.filesorter.model;

/**
 * Exception signalling that an editing operation on a comparison could not be performed.
 *
 * @author ugsrv
 */
public class ComparisonEditingException extends Exception {

    /**
     * Creates a new exception with the provided message.
     *
     * @param message the detail message
     */
    public ComparisonEditingException(String message) {
        super(message);
    }
}

