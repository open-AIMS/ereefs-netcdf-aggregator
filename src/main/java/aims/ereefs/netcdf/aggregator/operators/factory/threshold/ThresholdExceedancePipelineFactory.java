package aims.ereefs.netcdf.aggregator.operators.factory.threshold;

import aims.ereefs.netcdf.aggregator.operators.factory.AbstractOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Comparators;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Extends the {@link AbstractOperatorFactory} to provide support for {@code ThresholdExceedance}
 * {@link Pipeline}s via the {@link ThresholdExceedanceFactoryAdaptor} interface.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedancePipelineFactory extends AbstractOperatorFactory {

    /**
     * A list of all supported {@link ThresholdExceedanceFactoryAdaptor} implementations.
     */
    static protected List<ThresholdExceedanceFactoryAdaptor> supportedAdaptors =
        new ArrayList<ThresholdExceedanceFactoryAdaptor>() {
            {
                add(new SpeedThresholdExceedanceCountFactoryAdaptor());
                add(new SpeedThresholdExceedanceFrequencyFactoryAdaptor());
                add(new ThresholdExceedanceCountFactoryAdaptor());
                add(new ThresholdExceedanceFrequencyFactoryAdaptor());
                add(new ThresholdExceedanceValueAccumulationFactoryAdaptor());
                add(new ThresholdExceedanceValueSquaredAccumulationFactoryAdaptor());
            }
        };

    /**
     * Convenience method to find the {@link #supportedAdaptors Adaptor} that supports
     * instantiation of the specified {@code operatorType}.
     */
    protected Optional<ThresholdExceedanceFactoryAdaptor> findSupported(String operatorType) {
        return supportedAdaptors.stream()
            .filter(adaptor -> adaptor.supports(operatorType))
            .findFirst();
    }

    @Override
    public boolean supports(String operatorType) {
        return this.findSupported(operatorType).isPresent();
    }

    /**
     * Factory method that ultimately delegates to {@link ThresholdExceedanceFactoryAdaptor}
     * specialisations for instantiating a {@link Pipeline}.
     *
     * @return the instantiated {@link Pipeline}.
     * @see #supportedAdaptors
     */
    @Override
    public Pipeline make() {

        // Identify the operator.
        final NcAggregateProductDefinition.ThresholdSummaryOperator thresholdSummaryOperator =
            (
                this.summaryOperator instanceof NcAggregateProductDefinition.ThresholdSummaryOperator ?
                    (NcAggregateProductDefinition.ThresholdSummaryOperator) this.summaryOperator :
                    null
            );
        if (thresholdSummaryOperator == null) {
            throw new RuntimeException("Configuration object not supported by this factory. " +
                "Expected: \"" + NcAggregateProductDefinition.ThresholdSummaryOperator.class.getName() + "\"; " +
                "Was: \"" + this.summaryOperator.getClass().getName() + "\"."
            );
        }

        final Optional<ThresholdExceedanceFactoryAdaptor> optional = this.findSupported(thresholdSummaryOperator.getOperatorType());
        if (!optional.isPresent()) {
            throw new RuntimeException("Summary operator \"" + thresholdSummaryOperator.getOperatorType() + "\"not supported.");
        }
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = optional.get();

        // Typecast the operator.
        NcAggregateProductDefinition.ThresholdValueSummaryOperator thresholdValueSummaryOperator =
            (
                this.summaryOperator instanceof NcAggregateProductDefinition.ThresholdValueSummaryOperator ?
                    (NcAggregateProductDefinition.ThresholdValueSummaryOperator) this.summaryOperator :
                    null
            );
        NcAggregateProductDefinition.ThresholdZonalSummaryOperator thresholdZonalSummaryOperator =
            (
                this.summaryOperator instanceof NcAggregateProductDefinition.ThresholdZonalSummaryOperator ?
                    (NcAggregateProductDefinition.ThresholdZonalSummaryOperator) this.summaryOperator :
                    null
            );

        // Identify the comparator to use. Default to GREATER THAN.
        BiPredicate<Double, Double> thresholdComparator = Comparators.GREATER_THAN_COMPARATOR;
        final String comparatorStr = thresholdSummaryOperator.getComparator();
        if (comparatorStr != null) {
            if (comparatorStr.equalsIgnoreCase("greater")) {
                thresholdComparator = Comparators.GREATER_THAN_COMPARATOR;
                this.logger.debug("Using GREATER_THAN_COMPARATOR");
            }
            if (comparatorStr.equalsIgnoreCase("less")) {
                thresholdComparator = Comparators.LESS_THAN_COMPARATOR;
                this.logger.debug("Using LESS_THAN_COMPARATOR");
            }
        }

        // Is there only a single value for threshold?
        if (thresholdValueSummaryOperator != null) {

            // Attempt to instantiate a single-zone threshold operator.
            this.logger.debug("Single zone.");

            Double threshold = thresholdValueSummaryOperator.getThreshold();
            this.logger.debug("threshold: " + threshold);
            if (threshold == null) {
                throw new RuntimeException("Invalid configuration.");
            }
            return factoryAdaptor.make(
                thresholdSummaryOperator.getAccumulationTimeSlices(),
                threshold,
                thresholdComparator
            );

        }

        // Is there zonal values for threshold?
        if (thresholdZonalSummaryOperator != null) {

            // Attempt to instantiate a multi-zone threshold operator.
            this.logger.debug("Multiple zones");

            // Retrieve the indexToZoneIdMap from the registry.
            String bindName = thresholdZonalSummaryOperator.getZonesInputId();
            logger.debug("Retrieve indexToZoneIdMap from applicationContext at \"" + bindName + "\".");
            final List<String> indexToZoneIdMap = (List) applicationContext.getFromCache(bindName);

            // Retrieve the zoneIdToThresholdMap from the registry.
            bindName = thresholdZonalSummaryOperator.getThresholdsInputId();
            logger.debug("Retrieve zoneIdToThresholdMap from applicationContext at \"" + bindName + "\".");
            final Map<String, Double[]> zoneIdToThresholdMap =
                (Map<String, Double[]>) applicationContext.getFromCache(bindName);

            // Instantiate the threshold operator.
            return factoryAdaptor.make(
                thresholdSummaryOperator.getAccumulationTimeSlices(),
                indexToZoneIdMap,
                zoneIdToThresholdMap,
                thresholdComparator
            );

        }

        throw new RuntimeException("Unable to instantiate operator.");

    }

}
