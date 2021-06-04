package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.threshold.SpeedThresholdExceedanceCountFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceCountFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Comparators;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Tests for the {@link ThresholdExceedanceCountFactoryAdaptor}.
 *
 * @author Aaron Smith
 * @see ThresholdExceedanceOperatorHelper
 */
public class ThresholdExceedanceCountFactoryAdaptorTest {

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
                ThresholdExceedanceOperatorHelper.COUNT_CALCULATION
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceCountFactoryAdaptor();
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
                ThresholdExceedanceOperatorHelper.COUNT_CALCULATION
            );

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceCountFactoryAdaptor();
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
     * No accumulation period, single zone, greater than comparator, vector inputs.
     */
    @Test
    public void testNoAccumulationSingleZoneGreaterThanVector() {

        // Declare the test data.
        List<Double[]> testData = new ArrayList<Double[]>() {
            {
                // u
                add(new Double[]{
                    Double.NaN, 1.0,    // z = 1
                    2.0, 3.0,

                    4.0, 5.0,           // z = 2
                    6.0, 7.0
                });

                // v
                add(new Double[]{
                    7.0, 6.0,           // z = 1
                    5.0, 4.0,

                    3.0, 2.0,           // z = 2
                    1.0, null
                });
            }
        };

        // Declare the test parameters.
        int maxAccumulationTimeSlices = 1;
        double threshold = 5.1;

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            0.0, 1.0,   // z = 1
            1.0, 0.0,

            0.0, 1.0,   // z = 2
            1.0, 0.0
        };

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new SpeedThresholdExceedanceCountFactoryAdaptor();
        Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationTimeSlices,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );

        // Add the data.
        pipeline.execute(testData);

        // Validate the results.
        List<Double[]> resultsList = pipeline.getResults();
        Assertions
            .assertThat(resultsList.size())
            .isEqualTo(1);
        Double[] results = resultsList.get(0);
        Assertions
            .assertThat(expectedResults.length)
            .isEqualTo(results.length);
        for (int index = 0; index < expectedResults.length; index++) {
            // Remove precision from answers to avoid floating point errors.
            Double expectedResult = PipelineExecutionUtils.round(expectedResults[index]);
            Double actualResult = PipelineExecutionUtils.round(results[index]);
            Assertions
                .assertThat(actualResult)
                .as("index: %s", index)
                .isEqualTo(expectedResult);
        }

    }

    /**
     * No accumulation period, single zone, greater than comparator.
     */
    @Test
    public void testNoAccumulationSingleZoneGreaterThan() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 3.0,   // z = 1
            2.0, 2.0,

            2.0, 3.0,   // z = 2
            1.0, 3.0
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
            Double.NaN, 2.0,   // z = 1
            1.0, 1.0,

            1.0, 2.0,   // z = 2
            0.0, 2.0
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
            Double.NaN, 3.0,   // z = 1
            1.0, 2.0,

            2.0, 3.0,   // z = 2
            0.0, 2.0
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
            Double.NaN, 2.0,   // z = 1
            1.0, 1.0,

            1.0, 1.0,   // z = 2
            0.0, 1.0
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
            1.0, 2.0,

            2.0, 3.0,   // z = 2
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
            1.0, 1.0,

            1.0, 1.0,   // z = 2
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
