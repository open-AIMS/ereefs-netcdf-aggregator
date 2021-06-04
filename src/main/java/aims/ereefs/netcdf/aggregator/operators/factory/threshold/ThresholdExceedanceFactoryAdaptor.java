package aims.ereefs.netcdf.aggregator.operators.factory.threshold;

import aims.ereefs.netcdf.aggregator.operators.pipeline.Comparators;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Stage;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Public interface for instantiating a specialised {@code ThresholdExceedance} {@link Pipeline}.
 * Implementations of this interface are expected to only be invoked by
 * {@link ThresholdExceedancePipelineFactory}.
 */
public interface ThresholdExceedanceFactoryAdaptor {

    /**
     * Test to determine if the implementing {@code FactoryAdaptor} supports instantiation of the
     * specified {@link NcAggregateProductDefinition.SummaryOperator#getOperatorType()}.
     *
     * @param operatorType the type of operator to be instantiated.
     * @return {@code true} if the operator is supported, {@code false} otherwise.
     */
    boolean supports(String operatorType);

    /**
     * Instantiate a {@link Pipeline} to perform multi-zone {@code ThresholdExceedance}.
     *
     * @param maxAccumulationTimeSlices the number of {@link Stage#execute(List)} invocations over
     *                                  which to calculate the {@code Mean}.
     * @param indexToZoneIdMap          mapping of each cell to the corresponding {@code ZoneId}.
     * @param zoneIdToThresholdMap      mapping of {@code ZoneId} to a threshold value.
     * @param thresholdComparator       the {@code comparator} to apply to determine if the value
     *                                  exceeds the threshold. See
     *                                  {@link Comparators#GREATER_THAN_COMPARATOR}
     *                                  and {@link Comparators#LESS_THAN_COMPARATOR}.
     */
    Pipeline make(int maxAccumulationTimeSlices,
                  List<String> indexToZoneIdMap,
                  Map<String, Double[]> zoneIdToThresholdMap,
                  BiPredicate<Double, Double> thresholdComparator);

    /**
     * Instantiate a {@link Pipeline} to perform single-zone {@code ThresholdExceedance}.
     *
     * @param maxAccumulationTimeSlices the number of {@link Stage#execute(List)} invocations over
     *                                  which to calculate the {@code Mean}.
     * @param threshold                 the threshold value to apply.
     * @param thresholdComparator       the {@code comparator} to apply to determine if the value
     *                                  exceeds the threshold. See
     *                                  {@link Comparators#GREATER_THAN_COMPARATOR}
     *                                  and {@link Comparators#LESS_THAN_COMPARATOR}.
     */
    Pipeline make(int maxAccumulationTimeSlices,
                  double threshold,
                  BiPredicate<Double, Double> thresholdComparator);

}
