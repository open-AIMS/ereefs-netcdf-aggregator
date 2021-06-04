package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.MeanOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

/**
 * Test the {@code Mean} operator as implemented by the {@link Pipeline} built by
 * {@link MeanOperatorFactory}.
 *
 * @author Aaron Smith
 */
public class MeanOperatorTest {

    /**
     * Test under valid conditions.
     */
    @Test
    public void testValid() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
            Double.NaN, 2.35,   // z = 1
            2.05, 2.075,

            2.2, 5.0,   // z = 2
            0.825, 4.7
        };

        // Instantiate the Operator.
        PipelineFactory operatorFactory = new MeanOperatorFactory();
        Pipeline pipeline = operatorFactory.make();

        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);
        pipeline.reset();
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);

    }

}
