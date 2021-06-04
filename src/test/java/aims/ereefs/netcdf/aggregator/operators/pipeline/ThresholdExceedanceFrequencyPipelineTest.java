package aims.ereefs.netcdf.aggregator.operators.pipeline;

import aims.ereefs.netcdf.TestUtils;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.SpeedThresholdExceedanceFrequencyFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFrequencyFactoryAdaptor;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@code ThresholdExceedanceFrequency} {@link Pipeline} class.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedanceFrequencyPipelineTest {

    @Test
    public void testSpeed() {

        // Declare the test data.
        List<Double[]> testData = new ArrayList<Double[]>() {
            {
                // u
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0});

                // v
                add(new Double[]{7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, null});
            }
        };

        // Declare the test parameters.
        int maxAccumulationTimeSlices = 1;
        double threshold = 5.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new SpeedThresholdExceedanceFrequencyFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationTimeSlices,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );
        // Execute the calculations.
        pipeline.execute(testData);
        pipeline.execute(testData);

        // Validate the results.
        Double[] expectedResults = new Double[]{Double.NaN, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.NaN};
        List<Double[]> actualResults = pipeline.getResults();
        Assertions
            .assertThat(actualResults.size())
            .isEqualTo(1);
        TestUtils.assertSame(actualResults.get(0), expectedResults);

    }

    @Test
    public void testSimple() {

        // Declare the test data.
        List<Double[]> testData = new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0});
            }
        };

        // Declare the test parameters.
        int maxAccumulationTimeSlices = 1;
        double threshold = 2.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceFrequencyFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationTimeSlices,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );
        // Execute the calculations.
        pipeline.execute(testData);
        pipeline.execute(testData);

        // Validate the results.
        Double[] expectedResults = new Double[]{Double.NaN, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        List<Double[]> actualResults = pipeline.getResults();
        Assertions
            .assertThat(actualResults.size())
            .isEqualTo(1);
        TestUtils.assertSame(actualResults.get(0), expectedResults);

    }

}
