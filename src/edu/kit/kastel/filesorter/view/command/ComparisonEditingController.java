package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.ComparisonEditingException;
import edu.kit.kastel.filesorter.model.ComparisonEditor;
import edu.kit.kastel.filesorter.view.Arguments;
import edu.kit.kastel.filesorter.view.CommandExecuter;
import edu.kit.kastel.filesorter.view.EditingController;
import edu.kit.kastel.filesorter.view.InvalidArgumentException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Controller handling the interactive editing mode of a comparison.
 *
 * @author ugsrv
 */
public class ComparisonEditingController implements EditingController {

    private static final String ERROR_PREFIX = "Error: ";
    private static final String ERROR_TOO_MANY_ARGUMENTS = "too many arguments provided.";
    private static final String ERROR_INVALID_METRIC = "invalid metric.";
    private static final String ERROR_CONTEXT_NEGATIVE = "must be non-negative.";
    private static final String MESSAGE_EXIT = "OK, exit editing mode.";
    private static final String MATCH_FORMAT = "Match of length %d: %d-%d";
    private static final String STATUS_FORMAT = "Comparison of %s, %s: %s%% similarity, %d matches. "
            + "Available commands: matches, print, add, extend, truncate, discard, set, exit.";

    private final CommandExecuter<?, ?> executer;
    private final ComparisonEditor editor;
    private final PrintStream defaultStream;
    private final PrintStream errorStream;
    private ComparisonMetric metric = ComparisonMetric.SYMMETRIC;

    ComparisonEditingController(CommandExecuter<?, ?> executer, ComparisonEditor editor,
            PrintStream defaultStream, PrintStream errorStream) {
        this.executer = executer;
        this.editor = editor;
        this.defaultStream = defaultStream;
        this.errorStream = errorStream;
    }

    @Override
    public void onEnter() {
        printStatus();
    }

    @Override
    public boolean handleCommand(String[] command) {
        if (command.length == 0) {
            return false;
        }
        String keyword = command[0].toLowerCase(Locale.ROOT);
        return switch (keyword) {
            case "matches" -> handleMatches(command);
            case "print" -> handlePrint(command);
            case "add" -> handleAdd(command);
            case "extend" -> handleExtend(command);
            case "truncate" -> handleTruncate(command);
            case "discard" -> handleDiscard(command);
            case "set" -> handleSet(command);
            case "exit" -> handleExit(command);
            default -> false;
        };
    }

    private boolean handleMatches(String[] command) {
        if (command.length > 1) {
            printError(ERROR_TOO_MANY_ARGUMENTS);
            return true;
        }
        List<ComparisonEditor.MatchView> matches = this.editor.getMatches();
        for (ComparisonEditor.MatchView match : matches) {
            this.defaultStream.println(MATCH_FORMAT.formatted(match.length(), match.firstIndex(), match.secondIndex()));
        }
        printStatus();
        return true;
    }

    private boolean handlePrint(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            int matchIndex = arguments.parsePositive();
            int contextSize = 0;
            if (!arguments.isExhausted()) {
                int parsed = arguments.parseInteger();
                if (parsed < 0) {
                    throw new InvalidArgumentException("'%d' %s".formatted(parsed, ERROR_CONTEXT_NEGATIVE));
                }
                contextSize = parsed;
            }
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            ComparisonEditor.MatchContext context = this.editor.contextForMatch(matchIndex, contextSize);
            printContextLine(this.editor.firstIdentifier(), context.firstTokens(), context.firstMatchStart(), context.length());
            printContextLine(this.editor.secondIdentifier(), context.secondTokens(), context.secondMatchStart(), context.length());
            printStatus();
        } catch (InvalidArgumentException | ComparisonEditingException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleAdd(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            int firstIndex = arguments.parseInteger();
            int secondIndex = arguments.parseInteger();
            int length = arguments.parsePositive();
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            this.editor.addMatch(firstIndex, secondIndex, length);
            printStatus();
        } catch (InvalidArgumentException | ComparisonEditingException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleExtend(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            int matchIndex = arguments.parsePositive();
            int delta = arguments.parseInteger();
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            this.editor.extendMatch(matchIndex, delta);
            printStatus();
        } catch (InvalidArgumentException | ComparisonEditingException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleTruncate(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            int matchIndex = arguments.parsePositive();
            int delta = arguments.parseInteger();
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            this.editor.truncateMatch(matchIndex, delta);
            printStatus();
        } catch (InvalidArgumentException | ComparisonEditingException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleDiscard(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            int matchIndex = arguments.parsePositive();
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            this.editor.discardMatch(matchIndex);
            printStatus();
        } catch (InvalidArgumentException | ComparisonEditingException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleSet(String[] command) {
        Arguments arguments = createArguments(command);
        try {
            String metricName = arguments.parseString();
            if (!ensureNoExtraArguments(arguments)) {
                return true;
            }
            ComparisonMetric parsedMetric = ComparisonMetric.fromString(metricName);
            if (parsedMetric == null) {
                printError(ERROR_INVALID_METRIC);
                return true;
            }
            this.metric = parsedMetric;
            printStatus();
        } catch (InvalidArgumentException e) {
            printError(e.getMessage());
        }
        return true;
    }

    private boolean handleExit(String[] command) {
        Arguments arguments = createArguments(command);
        if (!ensureNoExtraArguments(arguments)) {
            return true;
        }
        this.defaultStream.println(MESSAGE_EXIT);
        this.executer.exitEditingMode();
        return true;
    }

    private Arguments createArguments(String[] command) {
        return new Arguments(Arrays.copyOfRange(command, 1, command.length));
    }

    private boolean ensureNoExtraArguments(Arguments arguments) {
        if (!arguments.isExhausted()) {
            printError(ERROR_TOO_MANY_ARGUMENTS);
            return false;
        }
        return true;
    }

    private void printContextLine(String identifier, List<String> tokens, int matchStart, int matchLength) {
        StringBuilder builder = new StringBuilder();
        builder.append(identifier).append(": ");
        int matchEnd = matchStart + matchLength;
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            if (i == matchStart) {
                builder.append('[');
            }
            builder.append(tokens.get(i));
            if (i + 1 == matchEnd) {
                builder.append(']');
            }
        }
        this.defaultStream.println(builder.toString());
    }

    private void printStatus() {
        double ratio = this.metric.compute(this.editor.totalMatchLength(),
                this.editor.firstTokenCount(), this.editor.secondTokenCount());
        String formatted = NumberFormatUtil.formatPercentage(ratio);
        String numeric = formatted.substring(0, formatted.length() - 1);
        this.defaultStream.println(STATUS_FORMAT.formatted(this.editor.firstIdentifier(),
                this.editor.secondIdentifier(), numeric, this.editor.matchCount()));
    }

    private void printError(String message) {
        if (message == null) {
            return;
        }
        this.errorStream.println(ERROR_PREFIX + message);
    }
}

