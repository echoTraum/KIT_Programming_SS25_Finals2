package edu.kit.kastel.filesorter.view.command;

import edu.kit.kastel.filesorter.model.AnalysisResult;
import edu.kit.kastel.filesorter.model.SequenceMatcher;
import edu.kit.kastel.filesorter.view.Command;
import edu.kit.kastel.filesorter.view.Result;

import java.util.List;

/**
 * Command that prints a histogram over similarity scores of the last analysis.
 */
public class Histogram implements Command<SequenceMatcher> {

    private static final int CLASS_COUNT = 10;
    private static final int CLASS_WIDTH_PERCENT = 10;
    private static final double PERCENTAGE_FACTOR = 100.0;
    private static final String ERROR_NO_ANALYSIS_RESULT = "No analysis result available.";
    private static final String ERROR_METRIC_NOT_PERCENTAGE = "Metric must be a percentage.";

    private final ListMetric metric;

    /**
     * Creates a new histogram command.
     *
     * @param metric the metric whose similarity values should be visualized
     */
    public Histogram(ListMetric metric) {
        this.metric = metric;
    }

    @Override
    public Result execute(SequenceMatcher handle) {
        AnalysisResult analysisResult = handle.getLastAnalysisResult();
        if (analysisResult == null) {
            return Result.error(ERROR_NO_ANALYSIS_RESULT);
        }
        if (!this.metric.isPercentage()) {
            return Result.error(ERROR_METRIC_NOT_PERCENTAGE);
        }

        List<PairSummary> summaries = PairSummaryCollector.collectSummaries(analysisResult);
        int[] buckets = new int[CLASS_COUNT];
        for (PairSummary summary : summaries) {
            double percentValue = this.metric.extract(summary) * PERCENTAGE_FACTOR;
            int bucketIndex = determineBucket(percentValue);
            buckets[bucketIndex]++;
        }

        StringBuilder builder = new StringBuilder();
        for (int bucket = CLASS_COUNT - 1; bucket >= 0; bucket--) {
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            int count = buckets[bucket];
            builder.append(':').append("|".repeat(count)).append(' ').append(count);
        }
        return Result.success(builder.toString());
    }

    private static int determineBucket(double percentValue) {
        if (percentValue < 0) {
            return 0;
        }
        if (percentValue >= CLASS_COUNT * CLASS_WIDTH_PERCENT) {
            return CLASS_COUNT - 1;
        }
        int bucketIndex = (int) (percentValue / CLASS_WIDTH_PERCENT);
        if (bucketIndex < 0) {
            return 0;
        }
        if (bucketIndex >= CLASS_COUNT) {
            return CLASS_COUNT - 1;
        }
        return bucketIndex;
    }
}

