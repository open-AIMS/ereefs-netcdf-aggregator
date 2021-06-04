package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.MinOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

/**
 * Integration test of the {@link Pipeline} created by the {@link MinOperatorFactory} to perform
 * a {@code Minimum} operation.
 *
 * @author Aaron Smith
 */
public class MinOperatorTest {

    /**
     * Expected results based on {@link TestData#TEST_DATA}.
     */
    final static public Double[] EXPECTED_RESULTS = new Double[]{  // results
        Double.NaN, 0.0,   // z = 1
        0.0, 0.0,

        0.0, 5.0,   // z = 2
        0.0, 5.0
    };

    /**
     * Execute the {@link Pipeline} created by the {@link MinOperatorFactory}.
     */
    @Test
    public void testValid() {

        Double[] expectedResults = MinOperatorTest.EXPECTED_RESULTS;

        // Instantiate the Operator.
        PipelineFactory operatorFactory = new MinOperatorFactory();
        Pipeline pipeline = operatorFactory.make();

        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);
        pipeline.reset();
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);

    }

}
