package aims.ereefs.netcdf.aggregator.operators.factory.threshold;

import aims.ereefs.netcdf.aggregator.operators.pipeline.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Concrete implementation of {@link ThresholdExceedanceFactoryAdaptor} for instantiating a
 * {@link Pipeline} to perform {@code ThresholdExceedanceValueSquaredAccumulation}.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedanceValueSquaredAccumulationFactoryAdaptor implements ThresholdExceedanceFactoryAdaptor {

    /**
     * Configuration constant identifying the type of {@code SummaryOperator} supported by this
     * factory.
     */
    static public final String VALUE_OPERATOR_TYPE = "THRESHOLD_VALUE_EXCEEDANCE_VALUE_SQUARED_ACCUMULATION";
    static public final String ZONAL_OPERATOR_TYPE = "THRESHOLD_ZONAL_EXCEEDANCE_VALUE_SQUARED_ACCUMULATION";
    public static final String[] SUPPORTED_OPERATOR_TYPES = new String[]{
        VALUE_OPERATOR_TYPE,
        ZONAL_OPERATOR_TYPE
    };

    @Override
    public boolean supports(String operatorType) {
        return Arrays.stream(SUPPORTED_OPERATOR_TYPES)
            .filter(value -> value.equalsIgnoreCase(operatorType))
            .findFirst()
            .isPresent();
    }

    @Override
    public Pipeline make(int maxAccumulationTimeSlices,
                         List<String> indexToZoneIdMap,
                         Map<String, Double[]> zoneIdToThresholdMap,
                         BiPredicate<Double, Double> thresholdComparator) {
        final SumCollectorStage sumCollectorStage = new SumCollectorStage();
        final ThresholdExceedanceValueSquaredStage thresholdExceedanceValueSquaredStage =
            new ThresholdExceedanceValueSquaredStage(
                indexToZoneIdMap,
                zoneIdToThresholdMap,
                thresholdComparator,
                new ArrayList<Stage>() {{
                    add(sumCollectorStage);
                }}
            );
        final MeanAccumulatorStage meanAccumulatorStage = new MeanAccumulatorStage(
            maxAccumulationTimeSlices,
            new ArrayList<Stage>() {{
                add(thresholdExceedanceValueSquaredStage);
            }}
        );
        return new BasicPipeline(meanAccumulatorStage, sumCollectorStage);
    }

    @Override
    public Pipeline make(int maxAccumulationTimeSlices,
                         double threshold,
                         BiPredicate<Double, Double> thresholdComparator) {
        final SumCollectorStage sumCollectorStage = new SumCollectorStage();
        final ThresholdExceedanceValueSquaredStage thresholdExceedanceValueSquaredStage = new ThresholdExceedanceValueSquaredStage(
            threshold,
            thresholdComparator,
            new ArrayList<Stage>() {{
                add(sumCollectorStage);
            }}
        );
        final MeanAccumulatorStage meanAccumulatorStage = new MeanAccumulatorStage(
            maxAccumulationTimeSlices,
            new ArrayList<Stage>() {{
                add(thresholdExceedanceValueSquaredStage);
            }}
        );
        return new BasicPipeline(meanAccumulatorStage, sumCollectorStage);
    }

}
