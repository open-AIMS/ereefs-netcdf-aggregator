package aims.ereefs.netcdf.aggregator.operators.pipeline;

import aims.ereefs.netcdf.TestUtils;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.SpeedThresholdExceedanceCountFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceCountFactoryAdaptor;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedanceFactoryAdaptor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@code ThresholdExceedanceCount} {@link Pipeline} class.
 *
 * @author Aaron Smith
 */
public class ThresholdExceedanceCountPipelineTest {

    @Test
    public void testSpeedSingleAccumulation() {

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
        int maxAccumulationCount = 1;
        double threshold = 5.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new SpeedThresholdExceedanceCountFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationCount,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );

        // Single execution.
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.NaN}
        );
        pipeline.reset();

        // Multiple execution.
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 2.0, 2.0, 0.0, 0.0, 2.0, 2.0, Double.NaN}
        );

    }

    @Test
    public void testSpeedMultiAccumulation() {

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
        int maxAccumulationCount = 2;
        double threshold = 5.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new SpeedThresholdExceedanceCountFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationCount,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );

        // Execute the calculations.
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.NaN}
        );
        pipeline.reset();

        pipeline.execute(testData);
        pipeline.execute(testData);
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 2.0, 2.0, 0.0, 0.0, 2.0, 2.0, Double.NaN}
        );

    }

    @Test
    public void testSimpleSingleAccumulation() {

        // Declare the test data.
        List<Double[]> testData = new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0});
            }
        };

        // Declare the test parameters.
        int maxAccumulationCount = 1;
        double threshold = 2.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceCountFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationCount,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );

        // Single execution.
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0}
        );
        pipeline.reset();

        // Multiple execution.
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 0.0, 0.0, 2.0, 2.0, 2.0, 2.0, 2.0}
        );

    }

    @Test
    public void testSimpleMultiAccumulation() {

        // Declare the test data.
        List<Double[]> testData = new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0});
            }
        };

        // Declare the test parameters.
        int maxAccumulationCount = 2;
        double threshold = 2.1;

        // Instantiate the Pipeline.
        final ThresholdExceedanceFactoryAdaptor factoryAdaptor = new ThresholdExceedanceCountFactoryAdaptor();
        final Pipeline pipeline = factoryAdaptor.make(
            maxAccumulationCount,
            threshold,
            Comparators.GREATER_THAN_COMPARATOR
        );

        // Execute the calculations.
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0}
        );
        pipeline.reset();

        pipeline.execute(testData);
        pipeline.execute(testData);
        pipeline.execute(testData);
        pipeline.execute(testData);
        TestUtils.assertSame(
            pipeline.getResults().get(0),
            new Double[]{Double.NaN, 0.0, 0.0, 2.0, 2.0, 2.0, 2.0, 2.0}
        );

    }

}
