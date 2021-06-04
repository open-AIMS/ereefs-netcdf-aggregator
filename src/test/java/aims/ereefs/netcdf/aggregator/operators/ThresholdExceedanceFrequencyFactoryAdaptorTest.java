package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFrequencyFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Comparators;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Tests for the {@link ThresholdExceedanceFrequencyFactoryAdaptor}.
 *
 * @author Aaron Smith
 * @see ThresholdExceedanceOperatorHelper
 */
public class ThresholdExceedanceFrequencyFactoryAdaptorTest {

    /**
     * Helper method to execute a single-zone threshold test using the specified data.
     */
    protected void executeSingleZoneThresholdTest(Map<Double, Double[]> testData,
                                                  Double threshold,
                                                  int maxAccumulationTimeSlices,
                                                  BiPredicate<Double, Double> thresholdComparator,
                                                  Double[] expectedResults) {

        // Calculate the results.
        Double[] calculatedResults =
            ThresholdExceedanceOperatorHelper.calculateResultsForExceedanceFrequency(
                testData,
                threshold,
                maxAccumulationTimeSlices,
                thresholdComparator
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceFrequencyFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationTimeSlices,
            threshold,
            thresholdComparator
        );

        // Execute the test and evaluate against calculated results.
        PipelineExecutionUtils.executeTestForSingleVariable(
            pipeline,
            testData,
            calculatedResults
        );

        // Reset the operator so the test can be re-executed.
        pipeline.reset();

        // If expected (calculated by hand) results are available, evaluate against them, otherwise
        // calculate again against calculated results.
        PipelineExecutionUtils.executeTestForSingleVariable(
            pipeline,
            testData,
            (expectedResults != null ? expectedResults : calculatedResults)
        );

    }

    /**
     * Helper method to execute a multi-zone threshold test using the specified data.
     */
    protected void executeMultiZoneThresholdTest(Map<Double, Double[]> testData,
                                                 List<String> indexToMultiZoneIdMap,
                                                 Map<String, Double[]> multiZoneIdToThresholdMap,
                                                 int maxAccumulationTimeSlices,
                                                 BiPredicate<Double, Double> thresholdComparator,
                                                 Double[] expectedResults) {

        // Calculate the threshold.
        Double[] multiZoneThreshold = ThresholdExceedanceOperatorHelper.calculateMultiZoneThreshold(
            indexToMultiZoneIdMap, multiZoneIdToThresholdMap);

        // Calculate the results.
        Double[] calculatedResults =
            ThresholdExceedanceOperatorHelper.calculateResultsForExceedanceFrequency(
                testData,
                multiZoneThreshold,
                maxAccumulationTimeSlices,
                thresholdComparator
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceFrequencyFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationTimeSlices,
            indexToMultiZoneIdMap,
            multiZoneIdToThresholdMap,
            thresholdComparator
        );

        // Execute the test and evaluate against calculated results.
        PipelineExecutionUtils.executeTestForSingleVariable(
            pipeline,
            testData,
            calculatedResults
        );

        // Reset the operator so the test can be re-executed.
        pipeline.reset();

        // If expected (calculated by hand) results are available, evaluate against them, otherwise
        // calculate again against calculated results.
        PipelineExecutionUtils.executeTestForSingleVariable(
            pipeline,
            testData,
            (expectedResults != null ? expectedResults : calculatedResults)
        );

    }

    /**
     * No accumulation period, single zone, greater than comparator.
     */
    @Test
    public void testNoAccumulationSingleZoneGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 0.75,   // z = 1
            0.5, 0.5,

            0.5, 0.75,   // z = 2
            0.25, 0.75
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeSingleZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.SINGLE_ZONE_THRESHOLD,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * No accumulation period, single zone, less than comparator.
     */
    @Test
    public void testNoAccumulationSingleZoneLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeSingleZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.SINGLE_ZONE_THRESHOLD,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

    /**
     * Accumulation period of two (2) time slices, single zone, greater than comparator.
     */
    @Test
    public void testWithAccumulationSingleZoneGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 1.0,   // z = 1
            0.5, 0.5,

            0.5, 1.0,   // z = 2
            0.0, 1.0
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeSingleZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.SINGLE_ZONE_THRESHOLD,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * Accumulation period of two (2) time slices, single zone, less than comparator.
     */
    @Test
    public void testWithAccumulationSingleZoneLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeSingleZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.SINGLE_ZONE_THRESHOLD,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

    /**
     * No accumulation period, multi zone, greater than comparator.
     */
    @Test
    public void testNoAccumulationMultiZoneGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 0.75,   // z = 1
            0.25, 0.5,

            0.5, 0.75,   // z = 2
            0.0, 0.5
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMap,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * No accumulation period, multi zone, less than comparator.
     */
    @Test
    public void testNoAccumulationMultiZoneLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMap,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

    /**
     * With accumulation period, single zone, greater than comparator.
     */
    @Test
    public void testWithAccumulationMultiZoneGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 1.0,   // z = 1
            0.5, 0.5,

            0.5, 0.5,   // z = 2
            0.0, 0.5
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMap,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * With accumulation period, single zone, less than comparator.
     */
    @Test
    public void testWithAccumulationMultiZoneLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMap,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

    /**
     * No accumulation period, multi zone, with some points not in a zone, greater than comparator.
     */
    @Test
    public void testNoAccumulationMultiZoneAndMissingZonesGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 0.0,   // z = 1
            0.25, 0.5,

            0.5, 0.75,   // z = 2
            0.0, 0.0
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMapWithMissingZones,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * No accumulation period, multi zone, with some points not in a zone, less than comparator.
     */
    @Test
    public void testNoAccumulationMultiZoneAndMissingZonesLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 1;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMapWithMissingZones,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

    /**
     * With accumulation period, multi zone, with some points not in a zone, greater than comparator.
     */
    @Test
    public void testWithAccumulationMultiZoneAndMissingZonesGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 0.0,   // z = 1
            0.5, 0.5,

            0.5, 0.5,   // z = 2
            0.0, 0.0
        };

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMapWithMissingZones,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.GREATER_THAN_COMPARATOR,
            expectedResults
        );

    }

    /**
     * With accumulation period, multi zone, with some points not in a zone, less than comparator.
     */
    @Test
    public void testWithAccumulationMultiZoneAndMissingZonesLessThan() {

        // Declare the populating parameters.
        int maxAccumulationTimeSlices = 2;

        // Execute the tests using the helper method.
        this.executeMultiZoneThresholdTest(
            ThresholdExceedanceOperatorHelper.TEST_DATA,
            ThresholdExceedanceOperatorHelper.indexToMultiZoneIdMapWithMissingZones,
            ThresholdExceedanceOperatorHelper.multiZoneIdToThresholdMap,
            maxAccumulationTimeSlices,
            Comparators.LESS_THAN_COMPARATOR,
            null
        );

    }

}
