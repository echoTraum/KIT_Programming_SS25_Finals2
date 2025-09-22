package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.model.TokenizationStrategy;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that triggers a text analysis for all loaded texts in the {@link SequenceMatcher}.
 *
 * @author ugsrv
 */
public class Analyze implements Command<SequenceMatcher> {

    private final TokenizationStrategy strategy;
    private final int minMatchLength;

    /**
     * Creates a new command instance.
     *
     * @param strategy the strategy to use for tokenizing the texts prior to the analysis
     * @param minMatchLength the minimum length a match must have to be considered
     */
    public Analyze(TokenizationStrategy strategy, int minMatchLength) {
        this.strategy = strategy;
        this.minMatchLength = minMatchLength;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        return handle.analyze(this.strategy, this.minMatchLength);
    }
}
