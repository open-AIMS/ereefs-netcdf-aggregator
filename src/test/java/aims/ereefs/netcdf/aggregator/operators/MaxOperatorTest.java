package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.MaxOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

/**
 * Integration test of the {@link Pipeline} created by the {@link MaxOperatorFactory} to perform
 * a {@code Maximum} operation.
 *
 * @author Aaron Smith
 */
public class MaxOperatorTest {

    /**
     * Expected results based on {@link TestData#TEST_DATA}.
     */
    final static public Double[] EXPECTED_RESULTS = new Double[]{  // results
        Double.NaN, 5.0,   // z = 1
        6.0, 5.0,

        4.4, 10.0,   // z = 2
        2.2, 7.8
    };

    /**
     * Test under valid conditions.
     */
    @Test
    public void testValid() {

        Double[] expectedResults = MaxOperatorTest.EXPECTED_RESULTS;

        // Instantiate the Operator.
        PipelineFactory operatorFactory = new MaxOperatorFactory();
        Pipeline pipeline = operatorFactory.make();

        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);
        pipeline.reset();
        PipelineExecutionUtils.executeTestForSingleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);

    }

}
