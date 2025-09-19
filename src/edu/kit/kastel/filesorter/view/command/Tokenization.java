package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.model.TokenizationResult;
import edu.kit.kastel.filesorter.model.TokenizationStrategy;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

/**
 * Command that retrieves the tokenization of a stored text from the {@link SequenceMatcher}.
 *
 * @author ugsrv
 */
public class Tokenization implements Command<SequenceMatcher> {

    private static final String TOKEN_SEPARATOR = "~";

    private final String identifier;
    private final TokenizationStrategy strategy;

    /**
     * Creates a new command instance.
     *
     * @param identifier the identifier of the stored text
     * @param strategy the strategy to use for tokenization
     */
    public Tokenization(String identifier, TokenizationStrategy strategy) {
        this.identifier = identifier;
        this.strategy = strategy;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        TokenizationResult result = handle.tokenize(this.identifier, this.strategy);
        if (!result.isSuccess()) {
            return Result.error(result.errorMessage());
        }
        return Result.success(String.join(TOKEN_SEPARATOR, result.tokens()));
    }
}
