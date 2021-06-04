package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Extends {@link AbstractThresholdExceedanceStage} to implement the {@code Value Squared} scenario
 * of {@code Threshold Exceedance}.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedanceValueSquaredStage extends AbstractThresholdExceedanceStage {

    public ThresholdExceedanceValueSquaredStage(List<String> indexToZoneIdMap,
                                                Map<String, Double[]> zoneIdToThresholdMap,
                                                BiPredicate<Double, Double> thresholdComparator,
                                                List<Stage> nextStages) {
        super(indexToZoneIdMap, zoneIdToThresholdMap, thresholdComparator, nextStages);
    }

    public ThresholdExceedanceValueSquaredStage(Double thresholdValue,
                                                BiPredicate<Double, Double> thresholdComparator,
                                                List<Stage> nextStages) {
        super(thresholdValue, thresholdComparator, nextStages);
    }

    @Override
    protected Double doThresholdComparison(double value, double threshold) {
        return this.thresholdComparator.test(value, threshold) ?
            Math.pow(Math.abs(value - threshold), 2) : 0.0;
    }

}
