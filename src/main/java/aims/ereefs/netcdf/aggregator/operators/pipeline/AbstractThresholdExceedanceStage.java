package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Convenient base class for {@code Threshold}-related {@code Pipeline} {@link Stage}s. Threshold
 * values can be specified at a global scale (all cells compared to the same threshold), or within
 * {@code Zones} (cells within the same zone are compared to the same threshold). To facilitate
 * this a global threshold value is treated as a threshold value for a single zone which encompasses
 * all cells.
 *
 * @author Aaron Smith
 */
abstract public class AbstractThresholdExceedanceStage extends AbstractIntermediateStage {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Mapping of each cell (index) to the corresponding {@code Zone}.
     */
    protected List<String> indexToZoneIdMap;

    /**
     * Mapping of a {@code Zone} to a threshold value. This supports seasonal thresholds, hence
     * the {@code Double[]} array.
     */
    protected Map<String, Double[]> zoneIdToThresholdMap;

    /**
     * Cached comparator function, specified in the constructor, for comparing a value against a
     * threshold.
     *
     * @see Comparators#GREATER_THAN_COMPARATOR
     * @see Comparators#LESS_THAN_COMPARATOR
     */
    protected BiPredicate<Double, Double> thresholdComparator;

    /**
     * Constructor to cache input parameters.
     */
    public AbstractThresholdExceedanceStage(List<String> indexToZoneIdMap,
                                            Map<String, Double[]> zoneIdToThresholdMap,
                                            BiPredicate<Double, Double> thresholdComparator,
                                            List<Stage> nextStages) {
        super(nextStages);
        this.indexToZoneIdMap = indexToZoneIdMap;
        this.zoneIdToThresholdMap = zoneIdToThresholdMap;
        this.thresholdComparator = thresholdComparator;
    }

    /**
     * Convenience constructor to cache input parameters.
     */
    public AbstractThresholdExceedanceStage(Double thresholdValue,
                                            BiPredicate<Double, Double> thresholdComparator,
                                            List<Stage> nextStages) {
        this(
            new ArrayList<String>() {
                {
                    add("singleZone");
                }
            },
            new HashMap<String, Double[]>() {
                {
                    put("singleZone", new Double[]{thresholdValue});
                }
            },
            thresholdComparator,
            nextStages
        );
    }

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        final List<Double[]> results = new ArrayList<>();
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            final Double[] inputArray = inputs.get(inputIndex);
            final Double[] resultArray = new Double[inputArray.length];
            results.add(resultArray);

            for (int dataIndex = 0; dataIndex < inputArray.length; dataIndex++) {

                // Identify the zone for the current cell. Use "mod" to seamlessly support a single
                // zone.
                final String zoneId =
                    this.indexToZoneIdMap.get(dataIndex % this.indexToZoneIdMap.size());

                // Do not process if the Zone cannot be determined (ie: no zone for that cell), or
                // the input data is a NaN.
                Double inputData = inputArray[dataIndex];
                if ((zoneId != null) && (inputData != null) && !Double.isNaN(inputData)) {

                    // Identify the threshold to use.
                    Double[] thresholds = this.zoneIdToThresholdMap.get(zoneId);
                    double threshold = thresholds[0];
                    /*
                    if (this.timeAggregatorHelper instanceof SeasonalTimeAggregatorHelper) {
                        SeasonalTimeAggregatorHelper seasonalTimeAggregatorHelper =
                            (SeasonalTimeAggregatorHelper) this.timeAggregatorHelper;
                        int seasonIndex =
                            seasonalTimeAggregatorHelper.findSeasonConfigIndexByDate(time);
                        threshold = thresholds[seasonIndex];
                    }
                     */

                    // Perform the comparison.
                    resultArray[dataIndex] = this.doThresholdComparison(
                        inputArray[dataIndex],
                        threshold
                    );
                } else {

                    // Not processed, so result is NaN.
                    resultArray[dataIndex] = Double.NaN;

                }

            }
        }

        // Invoke the next stages.
        this.executeNextStages(results);

    }

    /**
     * Template method to compare the mean value to the threshold, and handle accordingly.
     */
    abstract protected Double doThresholdComparison(double value, double threshold);

}
