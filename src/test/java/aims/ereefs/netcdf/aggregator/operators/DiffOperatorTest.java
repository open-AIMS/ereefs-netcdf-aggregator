package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.DifferenceOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

/**
 * Test the DIFF operator as implemented by the {@link Pipeline} built by
 * {@link DifferenceOperatorFactory}.
 *
 * @author Marc Hammerton
 */
public class DiffOperatorTest {

    /**
     * Test under valid conditions.
     */
    @Test
    public void testValid() {

        // Declare the result.
        Double[] expectedResults = new Double[]{  // results
                Double.NaN, 0.525,   // z = 1
                -0.425, -0.725,

                -0.225, 0.25,   // z = 2
                -0.05, -0.025
        };

        // Instantiate the Operator.
        PipelineFactory operatorFactory = new DifferenceOperatorFactory();
        Pipeline pipeline = operatorFactory.make();

        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForSingleVariableFromTwoInputs(pipeline,
                TestData.TEST_DATA,
                TestData.TEST_DATA2,
                expectedResults);
        pipeline.reset();
        PipelineExecutionUtils.executeTestForSingleVariableFromTwoInputs(pipeline,
                TestData.TEST_DATA,
                TestData.TEST_DATA2,
                expectedResults);

    }

}
