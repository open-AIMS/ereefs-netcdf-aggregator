package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceValueAccumulationFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Comparators;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Tests for the {@link ThresholdExceedanceValueAccumulationFactoryAdaptor}.
 *
 * @author Aaron Smith
 * @see ThresholdExceedanceOperatorHelper
 */
public class ThresholdExceedanceValueAccumulationFactoryAdaptorTest {

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
            ThresholdExceedanceOperatorHelper.calculateResultsForExceedance(
                testData,
                threshold,
                maxAccumulationTimeSlices,
                thresholdComparator,
                ThresholdExceedanceOperatorHelper.ACCUMULATION_CALCULATION
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceValueAccumulationFactoryAdaptor();
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
            ThresholdExceedanceOperatorHelper.calculateResultsForExceedance(
                testData,
                multiZoneThreshold,
                maxAccumulationTimeSlices,
                thresholdComparator,
                ThresholdExceedanceOperatorHelper.ACCUMULATION_CALCULATION
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceValueAccumulationFactoryAdaptor();
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
            Double.NaN, 11.5,   // z = 1
            9.2, 4.4,

            4.8, 14.0,   // z = 2
            0.2, 12.8
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
            Double.NaN, 4.75,   // z = 1
            3.5, 2.2,

            2.4, 6.0,   // z = 2
            0.0, 5.4
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
            Double.NaN, 11.5,   // z = 1
            8.0, 2.4,

            0.8, 8.0,   // z = 2
            0.0, 3.8
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
            Double.NaN, 4.75,   // z = 1
            2.5, 1.2,

            0.4, 3.5,   // z = 2
            0.0, 1.4
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
            8.0, 2.4,

            0.8, 8.0,   // z = 2
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
            2.5, 1.2,

            0.4, 3.5,   // z = 2
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
