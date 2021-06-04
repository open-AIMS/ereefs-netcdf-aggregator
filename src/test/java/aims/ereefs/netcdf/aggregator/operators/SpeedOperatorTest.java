package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.SpeedMeanOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.SpeedOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration tests for the {@link Pipeline} implementation instantiated by the
 * {@link SpeedOperatorFactory} and {@link SpeedMeanOperatorFactory}.
 *
 * @author Aaron Smith
 */
public class SpeedOperatorTest {

    /**
     * Constant value representing the date/time of "2016-03-01T00:00".
     */
    final static public double BASE_TIME = 9556.0;

    /**
     * Threshold to use for all tests.
     */
    private double threshold = 5.0;

    /**
     * Valid test data for the U variable consisting of multiple time slices for a 2 x 2 grid with
     * a height of 2.
     */
    private List<Double[]> uTestData = Arrays.asList(
        new Double[]{  // Time slice 1
            Double.NaN, -1.0, // z = 1
            -1.0, 1.0,

            null, 0.5,  // z = 2
            0.5, -0.5
        },
        new Double[]{  // Time slice 2
            Double.NaN, -0.1,  // z = 1
            null, 0.1,

            1.0, 1.0,   // z = 2
            null, 1.0
        },
        new Double[]{  // Time slice 3
            Double.NaN, 0.0,  // z = 1
            0.3, null,

            0.4, 1.0,   // z = 2
            0.0, null
        },
        new Double[]{  // Time slice 4
            Double.NaN, null,  // z = 1
            0.5, 0.5,

            0.4, 0.1,   // z = 2
            -1.1, 0.6
        }
    );

    /**
     * Valid test data for the V variable consisting of multiple time slices for a 2 x 2 grid with
     * a height of 2.
     */
    private List<Double[]> vTestData = Arrays.asList(
        new Double[]{  // Time slice 1
            Double.NaN, -1.0, // z = 1
            -1.0, 1.0,

            0.1, 2.5,  // z = 2
            2.5, -2.5
        },
        new Double[]{  // Time slice 2
            Double.NaN, -3.1,  // z = 1
            null, 3.1,

            2.0, 2.0,   // z = 2
            null, 2.0
        },
        new Double[]{  // Time slice 3
            Double.NaN, 1.0,  // z = 1
            1.3, null,

            2.4, 2.0,   // z = 2
            2.0, null
        },
        new Double[]{  // Time slice 4
            Double.NaN, null,  // z = 1
            3.5, 3.5,

            2.4, null,  // z = 2
            -2.1, 2.6
        }
    );

    /**
     * Perform a test with valid data.
     */
    @Test
    public void testValid() {

        // Declare the result.
        Double[] expectedMinimum = new Double[]{  // results
            Double.NaN, 1.0,    // z = 1
            1.33, 1.41,

            2.24, 2.24,   // z = 2
            2.0, 2.24
        };
        Double[] expectedMean = new Double[]{  // results
            Double.NaN, 1.38,   // z = 1
            1.57, 2.01,

            1.78, 1.76,   // z = 2
            1.73, 1.86
        };
        Double[] expectedMaximum = new Double[]{  // results
            Double.NaN, 3.10,   // z = 1
            3.54, 3.54,

            2.43, 2.55,   // z = 2
            2.55, 2.67
        };

        // Instantiate the Pipelines.
        PipelineFactory speedOperatorFactory = new SpeedOperatorFactory();
        Pipeline speedPipeline = speedOperatorFactory.make();
        PipelineFactory speedMeanOperatorFactory = new SpeedMeanOperatorFactory();
        Pipeline speedMeanPipeline = speedMeanOperatorFactory.make();

        // Build the test data.
        List<List<Double[]>> combinedTestData = new ArrayList<>(2);
        combinedTestData.add(this.uTestData);
        combinedTestData.add(this.vTestData);

        // Build the list of results.
        List<Double[]> expectedSpeedResults = new ArrayList(3) {{
            add(expectedMinimum);
            add(expectedMean);
            add(expectedMaximum);
        }};
        List<Double[]> expectedSpeedMeanResults = new ArrayList(3) {{
            add(expectedMean);
        }};


        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForMultipleVariable(speedPipeline, combinedTestData,
            expectedSpeedResults);
        speedPipeline.reset();
        PipelineExecutionUtils.executeTestForMultipleVariable(speedPipeline, combinedTestData,
            expectedSpeedResults);

        PipelineExecutionUtils.executeTestForMultipleVariable(speedMeanPipeline, combinedTestData,
            expectedSpeedMeanResults);
        speedMeanPipeline.reset();
        PipelineExecutionUtils.executeTestForMultipleVariable(speedMeanPipeline, combinedTestData,
            expectedSpeedMeanResults);

    }

    /**
     * Perform a test with invalid data.
     */
    @Test
    public void testInvalidTimeSliceListCount() {

        // Instantiate the Pipelines.
        PipelineFactory speedOperatorFactory = new SpeedOperatorFactory();
        Pipeline speedPipeline = speedOperatorFactory.make();
        PipelineFactory speedMeanOperatorFactory = new SpeedMeanOperatorFactory();
        Pipeline speedMeanPipeline = speedMeanOperatorFactory.make();

        // Build the test data.
        List<Double[]> combinedTestData = new ArrayList<>(2);
        combinedTestData.add(new Double[0]);
        combinedTestData.add(new Double[0]);

        // Add extra test data which should cause the fail.
        combinedTestData.add(new Double[0]);

        // Execute the test, reset, and re-execute the test.
        try {
            speedPipeline.execute(combinedTestData);
            Assertions.fail("Expected exception.");
        } catch (Exception e) {
            Assertions
                .assertThat(e)
                .isInstanceOf(RuntimeException.class);
        }
        try {
            speedMeanPipeline.execute(combinedTestData);
            Assertions.fail("Expected exception.");
        } catch (Exception e) {
            Assertions
                .assertThat(e)
                .isInstanceOf(RuntimeException.class);
        }

    }

}
