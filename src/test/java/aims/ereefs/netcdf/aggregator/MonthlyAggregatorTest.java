package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactoryFactory;
import aims.ereefs.netcdf.aggregator.time.MonthlyTimeAggregatorHelper;
import aims.ereefs.netcdf.util.DateUtils;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index4D;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link aims.ereefs.netcdf.aggregator.MonthlyAggregator} class.
 *
 * @author Aaron Smith
 */
public class MonthlyAggregatorTest {

    /**
     * Constant value representing the date/time of "2016-03-01T00:00".
     */
    final static public double BASE_TIME = 9556.0;

    /**
     * Constant value representing a single hour.
     */
    final static public double HOUR = 1.0 / 24.0;

    /**
     * Helper method to instantiate a {@link MonthlyAggregator}.
     */
    protected MonthlyAggregator getAggregator() {
        MonthlyAggregator aggregator = null;
        try {
            // Instantiate the Aggregator with "default" values.
            DateUnit dateUnit = DateUtils.getDateUnit();
            aggregator = new MonthlyAggregator(new MonthlyTimeAggregatorHelper(dateUnit));

            // Configure it to use a Mean operator.
            NcAggregateProductDefinition.SummaryOperator meanConfig = NcAggregateProductDefinition.SummaryOperator.makeMean("temp");
            aggregator.setPipelineFactory(PipelineFactoryFactory.make(meanConfig, null));

            // Set the DataType to Double.
            aggregator.setDataType(DataType.DOUBLE);

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
        return aggregator;
    }

    /**
     * Invoke {@link AbstractAggregator#add(double, List)} before invoking
     * {@link AbstractAggregator#initialise()}.
     */
    @Test
    public void testAddWithoutInitialising() {
        MonthlyAggregator aggregator = this.getAggregator();

        try {
            Assertions
                .assertThat(aggregator.isInitialised())
                .isFalse();
            aggregator.add(BASE_TIME, new ArrayList<>());
            Assertions
                .fail("Exception expected.");
        } catch (Exception e) {
            Assertions
                .assertThat(e)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No operator specified.");

        }

    }

    /**
     * Test {@link MonthlyAggregator#add(double, List)} with valid data. Data will be hourly for
     * two (2) days, at 2 depths, on a 2 x 2 grid, and will use a Mean aggregation.
     */
    @Test
    public void testMonthlyAddValid() {

        int[] shape = new int[]{1, 2, 2, 2};

        // Instantiate the Aggregator and Operator.
        MonthlyAggregator aggregator = this.getAggregator();
        aggregator.setShape(shape);
        Assertions
            .assertThat(aggregator.isInitialised())
            .isFalse();
        aggregator.initialise();
        Assertions
            .assertThat(aggregator.isInitialised())
            .isTrue();

        // Calculate mean average while building the dataset.
        double total = 0.0;
        int count = 0;

        // Use a 4D index to track location when writing to Array.
        Index timeSliceIndex = new Index4D(shape);

        // Build the dataset. Each entry represents 1 hour.
        List<Double[]> variableDataArrayList = new ArrayList<>(60 * 24);
        for (int day = 0; day < 2; day++) { // 2 days.
            for (int hour = 0; hour < 24; hour++) { // 24 hours per day.

                // Create an 4D array for each time slice.
                Double[] variableDataArray = new Double[shape[0] * shape[1] * shape[2] * shape[3]];
                int timeIndex = day * 24 + hour;
                double value = timeIndex; // Same value for entire time slice.
                for (int depthIndex = 0; depthIndex < 2; depthIndex++) {
                    for (int x = 0; x < 2; x++) {
                        for (int y = 0; y < 2; y++) {

                            // Write the value to the array.
                            variableDataArray[timeSliceIndex.set(0, depthIndex, x, y).currentElement()] = value;

                            // Cache the value for calculating the mean average.
                            total += value;
                            count++;

                        }
                    }
                }

                // Cache the time slice.
                variableDataArrayList.add(variableDataArray);
            }
        }

        // Add the data to the Aggregator.
        double time = BASE_TIME;
        for (Double[] variableDataArray : variableDataArrayList) {
            List<Double[]> list = new ArrayList<>();
            list.add(variableDataArray);
            aggregator.add(time, list);
            time += 1 / 24;
        }

        // Verify the results.
        List<Double[]> resultsList = aggregator.getAggregatedData();

        // Only one variable.
        Assertions
            .assertThat(resultsList)
            .hasSize(1);
        Double[] results = resultsList.get(0);
        Assertions
            .assertThat(results.length)
            .isEqualTo(8);

        // Each value matches the expected result.
        final double mean = total / count;
        Assertions
            .assertThat(mean)
            .isGreaterThan(0.0);
        for (int index = 0; index < 8; index++) {
            Assertions
                .assertThat(results[index])
                .isEqualTo(mean);
        }

        // Un-initialise.
        Assertions
            .assertThat(aggregator.isInitialised())
            .isTrue();
        aggregator.unInitialise();
        Assertions
            .assertThat(aggregator.isInitialised())
            .isFalse();

    }

}
