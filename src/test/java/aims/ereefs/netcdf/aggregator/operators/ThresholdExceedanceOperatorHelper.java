package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import aims.ereefs.netcdf.util.DateUtils;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Constants and shared utilities for testing concrete {@link ThresholdExceedanceFactoryAdaptor}
 * classes.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedanceOperatorHelper {

    /**
     * Test data representing multiple time slices four (4) time slices (days from 29-Sept-2016) for
     * a 2 x 2 grid with a height of 2. {@code null} values should be treated as {@code NaN}.
     */
    final static public Map<Double, Double[]> TEST_DATA = new TreeMap<>();

    static {
        double startDateTime = NetcdfDateUtils.fromLocalDateTime(
            DateUtils.getDateUnit(), LocalDateTime.of(2016, 9, 29, 0, 0));

        // Day 1.
        TEST_DATA.put(
            startDateTime + 0.0,
            new Double[]{
                Double.NaN, 10.0,   // z = 1
                0.0, null,

                null, 10.0, // z = 2
                2.2, 7.8
            }
        );

        // Day 2.
        TEST_DATA.put(
            startDateTime + 1.0,
            new Double[]{
                Double.NaN, null,  // z = 1
                2.2, 0.0,

                0.0, 5.0,   // z = 2
                null, 5.0
            }
        );

        // Day 3.
        TEST_DATA.put(
            startDateTime + 2.0,
            new Double[]{
                Double.NaN, 5.0,  // z = 1
                11.0, 3.3,

                4.4, 5.0,   // z = 2
                0.0, null
            }
        );

        // Day 4.
        TEST_DATA.put(
            startDateTime + 3.0,
            new Double[]{
                Double.NaN, 2.5,   // z = 1
                null, 5.1,

                4.4, null,  // z = 2
                1.1, 6.0
            }
        );
    }

    /**
     * {@code IndexToZoneIdMap} for a single zone. This property is accessible by all other classes
     * in this package.
     */
    final static double SINGLE_ZONE_THRESHOLD = 2.0;

    /**
     * <code>IndexToZoneIdMap</code> for multiple zones, matching the number of entries in
     * {@link #TEST_DATA}.  This property is accessible by all other classes in this package.
     */
    final static List<String> indexToMultiZoneIdMap = new ArrayList<>();

    static {
        indexToMultiZoneIdMap.add("zone1");
        indexToMultiZoneIdMap.add("zone1");
        indexToMultiZoneIdMap.add("zone2");
        indexToMultiZoneIdMap.add("zone2");
        indexToMultiZoneIdMap.add("zone3");
        indexToMultiZoneIdMap.add("zone3");
        indexToMultiZoneIdMap.add("zone4");
        indexToMultiZoneIdMap.add("zone4");
    }

    /**
     * <code>IndexToZoneIdMap</code> for multiple zones where some entries do not fall within a
     * zone (ie: are <code>null</code>). This property is accessible by all other classes in this
     * package.
     */
    final static List<String> indexToMultiZoneIdMapWithMissingZones = new ArrayList<>();

    static {
        indexToMultiZoneIdMapWithMissingZones.add("zone1");
        indexToMultiZoneIdMapWithMissingZones.add(null);
        indexToMultiZoneIdMapWithMissingZones.add("zone2");
        indexToMultiZoneIdMapWithMissingZones.add("zone2");
        indexToMultiZoneIdMapWithMissingZones.add("zone3");
        indexToMultiZoneIdMapWithMissingZones.add("zone3");
        indexToMultiZoneIdMapWithMissingZones.add("zone4");
        indexToMultiZoneIdMapWithMissingZones.add(null);

    }

    /**
     * <code>IndexToZoneIdMap</code> for multiple zones, matching the number of zones in
     * {@link #indexToMultiZoneIdMap}. This property is accessible by all other classes in this
     * package.
     */
    final static Map<String, Double[]> multiZoneIdToThresholdMap = new HashMap<>();

    static {
        multiZoneIdToThresholdMap.put("zone1", new Double[]{2.0});
        multiZoneIdToThresholdMap.put("zone2", new Double[]{3.0});
        multiZoneIdToThresholdMap.put("zone3", new Double[]{4.0});
        multiZoneIdToThresholdMap.put("zone4", new Double[]{5.0});
    }

    /**
     * Helper method to merge an <code>indexToMultiZoneIdMap</code> and
     * <code>multiZoneIdToThresholdMap</code> to generate a simplified threshold array. This
     * property is accessible by all other classes in this package.
     */
    static Double[] calculateMultiZoneThreshold(List<String> indexToMultiZoneIdMap,
                                                Map<String, Double[]> multiZoneIdToThresholdMap) {
        Double[] thresholds = new Double[indexToMultiZoneIdMap.size()];
        int index = 0;
        for (String zoneId : indexToMultiZoneIdMap) {
            thresholds[index] = (zoneId == null ? Double.NaN : multiZoneIdToThresholdMap.get(zoneId)[0]);
            index++;
        }
        return thresholds;
    }

    /**
     * Implement a <code>Count</code> function which always returns <code>1.0</code>. This assumes
     * that a <code>comparator</code> has already confirmed <code>value</code> exceeds
     * <code>threshold</code>.
     */
    static public BiFunction<Double, Double, Double> COUNT_CALCULATION = (value, threshold) -> 1.0;

    /**
     * Implement an <code>Accumulation</code> function which returns the absolute difference
     * between <code>value</code> and <code>threshold</code>.
     */
    static public BiFunction<Double, Double, Double> ACCUMULATION_CALCULATION =
        (value, threshold) -> Math.abs(value - threshold);

    /**
     * Calculate exceedance of <code>testData</code> compared to the single-zone
     * <code>thresholds</code> using the specified <code>comparator</code> and applying the
     * specified <code>calculation</code>.
     */
    static public Double[] calculateResultsForExceedance(Map<Double, Double[]> testData,
                                                         Double threshold,
                                                         int maxAccumulationTimeSlices,
                                                         BiPredicate<Double, Double> comparator,
                                                         BiFunction<Double, Double, Double> calculation) {
        // Expand the thresholds to an array of matching size.
        int layerLength = testData.get(testData.keySet().iterator().next()).length;
        Double[] thresholds = new Double[layerLength];
        Arrays.fill(thresholds, threshold);

        return calculateResultsForExceedance(
            testData,
            thresholds,
            maxAccumulationTimeSlices,
            comparator,
            calculation
        );
    }

    /**
     * Calculate exceedance of <code>testData</code> compared to the multi-zone
     * <code>thresholds</code> using the specified <code>comparator</code> and applying the
     * specified <code>calculation</code>.
     */
    static public Double[] calculateResultsForExceedance(Map<Double, Double[]> testData,
                                                         Double[] thresholds,
                                                         int maxAccumulationTimeSlices,
                                                         BiPredicate<Double, Double> comparator,
                                                         BiFunction<Double, Double, Double> calculation) {

        // Perform the accumulation first, generating an intermediary list for later comparison and
        // calculation.
        List<Double[]> accumulatedDataList = new ArrayList<>();
        Double[] accumulatedData = null;
        int accumulationCount = 0;
        for (Double time : testData.keySet()) {
            Double[] timeSlice = testData.get(time);

            // If this is the start of a new accumulation period, create a new accumulator and add
            // it to the list.
            if (accumulationCount == 0) {
                accumulatedData = new Double[timeSlice.length];
                Arrays.fill(accumulatedData, Double.NaN);
                accumulatedDataList.add(accumulatedData);
            }

            // Accumulate each value in the time slice.
            for (int index = 0; index < timeSlice.length; index++) {
                Double value = timeSlice[index];

                // Confirm value is a valid number before accumulating.
                if ((value != null) && (!Double.isNaN(value))) {

                    // Set NaN to zero for accumulation to succeed.
                    if (Double.isNaN(accumulatedData[index])) {
                        accumulatedData[index] = 0.0;
                    }
                    accumulatedData[index] += value;
                }
            }
            accumulationCount++;

            // Has accumulation completed?
            if (accumulationCount >= maxAccumulationTimeSlices) {
                // Reset accumulation.
                accumulatedData = null;
                accumulationCount = 0;
            }
        }

        // Compare the accumulated values to the threshold and perform calculation.
        Double[] results = new Double[accumulatedDataList.get(0).length];
        Arrays.fill(results, Double.NaN);
        for (Double[] data : accumulatedDataList) {

            // Process each value.
            for (int index = 0; index < data.length; index++) {

                // Value is the mean of the accumulation.
                Double value = data[index] / maxAccumulationTimeSlices;
                Double threshold = thresholds[index];

                // Only process if both the value and threshold are not NaN.
                if (!Double.isNaN(value) && !Double.isNaN(threshold)) {

                    // Initialise the result to zero if necessary. This MUST be done before
                    // comparator test as the result should be zero even if the test fails, since
                    // performing the test is valid.
                    if (Double.isNaN(results[index])) {
                        results[index] = 0.0;
                    }

                    // Does it pass the comparator test?
                    if (comparator.test(value, threshold)) {

                        // Accumulated value exceeds threshold, so perform calculation.
                        results[index] += calculation.apply(value, threshold);
                    }
                }
            }
        }

        return results;
    }

    /**
     * Calculate the expected results for frequency of exceedance with a single-zone threshold.
     */
    static public Double[] calculateResultsForExceedanceFrequency(Map<Double, Double[]> testData,
                                                                  Double threshold,
                                                                  int maxAccumulationTimeSlices,
                                                                  BiPredicate<Double, Double> comparator) {
        // Expand the thresholds to an array of matching size.
        int layerLength = testData.get(testData.keySet().iterator().next()).length;
        Double[] thresholds = new Double[layerLength];
        Arrays.fill(thresholds, threshold);

        return calculateResultsForExceedanceFrequency(
            testData,
            thresholds,
            maxAccumulationTimeSlices,
            comparator
        );
    }

    /**
     * Calculate the expected results for frequency of exceedance with a multi-zone threshold.
     */
    static public Double[] calculateResultsForExceedanceFrequency(Map<Double, Double[]> testData,
                                                                  Double[] thresholds,
                                                                  int maxAccumulationTimeSlices,
                                                                  BiPredicate<Double, Double> comparator) {
        // Calculate the counts of exceedance for the parameters provided.
        Double[] countsOfExceedance = calculateResultsForExceedance(
            testData,
            thresholds,
            maxAccumulationTimeSlices,
            comparator,
            COUNT_CALCULATION
        );

        // Calculate the number of data points represented, based on the size of the testData and
        // the number of accumulations.
        double numberOfDataPoints = testData.size() / maxAccumulationTimeSlices;

        // Convert the counts to frequency of exceedance.
        Double[] frequencyOfExceedance = new Double[8];
        Arrays.fill(frequencyOfExceedance, Double.NaN);
        for (int index = 0; index < countsOfExceedance.length; index++) {
            frequencyOfExceedance[index] = (double) countsOfExceedance[index] / numberOfDataPoints;
        }

        return frequencyOfExceedance;
    }

}
