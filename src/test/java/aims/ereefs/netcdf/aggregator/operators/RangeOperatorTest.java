package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.RangeOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.junit.Test;

import java.util.*;

/**
 * Integration tests for the {@link Pipeline} implementation instantiated by the
 * {@link RangeOperatorFactory}.
 *
 * @author Aaron Smith
 */
public class RangeOperatorTest {

    /**
     * Expected results based on {@link TestData#TEST_DATA}.
     */
    final static public Double[] EXPECTED_RESULTS = new Double[] {  // results
        Double.NaN, 5.0,   // z = 1
        6.0, 5.0,

        4.4, 5.0,   // z = 2
        2.2, 2.8
    };

    /**
     * Perform a test with valid data.
     */
    @Test
    public void testValid() {

        List<Double[]> expectedResults = new ArrayList<>();
        expectedResults.add(MinOperatorTest.EXPECTED_RESULTS);
        expectedResults.add(MaxOperatorTest.EXPECTED_RESULTS);
        expectedResults.add(RangeOperatorTest.EXPECTED_RESULTS);

        // Instantiate the Operator.
        PipelineFactory operatorFactory = new RangeOperatorFactory();
        Pipeline pipeline = operatorFactory.make();

        // Execute the test, reset, and re-execute the test.
        PipelineExecutionUtils.executeTestForMultipleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);
        pipeline.reset();
        PipelineExecutionUtils.executeTestForMultipleVariable(pipeline, TestData.TEST_DATA,
            expectedResults);

    }

}
