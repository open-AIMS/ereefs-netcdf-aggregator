package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic utilities for executing tests against a {@link Pipeline}.
 *
 * @author Aaron Smith
 */
public class PipelineExecutionUtils {

    /**
     * Perform the actual test on a {@link Pipeline} that handles a single variable only.
     *
     * @param pipeline        the {@link Pipeline} to execute.
     * @param testData        data for one or more variables to pass to the {@code Pipeline}.
     * @param expectedResults the results expected from the {@code Pipeline}.
     */
    final static public void executeTestForSingleVariable(Pipeline pipeline,
                                                          Map<Double, Double[]> testData,
                                                          Double[] expectedResults) {

        // Ensure data counts match.
        Assertions
            .assertThat(testData.size())
            .isNotZero();
        final int singleVariableDataLength = testData.get(testData.keySet().iterator().next()).length;
        Assertions
            .assertThat(singleVariableDataLength)
            .isEqualTo(expectedResults.length);

        // Loop through each time slice.
        for (Double time : testData.keySet()) {
            Double[] data = testData.get(time);
            List<Double[]> variableDataWrapper = new ArrayList<Double[]>();
            variableDataWrapper.add(data);
            pipeline.execute(variableDataWrapper);
        }

        // Validate the results.
        final List<Double[]> resultsList = pipeline.getResults();
        Assertions
            .assertThat(resultsList.size())
            .isEqualTo(1);
        final Double[] results = resultsList.get(0);
        Assertions
            .assertThat(singleVariableDataLength)
            .isEqualTo(results.length);
        for (int index = 0; index < singleVariableDataLength; index++) {
            // Remove precision from answers to avoid floating point errors.
            final Double expectedResult = expectedResults[index];
            final Double actualResult = results[index];
            if ((expectedResult == null) || (Double.isNaN(expectedResult))) {

                // Compare nulls and NaNs.
                Assertions
                    .assertThat(actualResult)
                    .as("index: %s", index)
                    .isEqualTo(expectedResult);

            } else {

                // Compare actual values.
                Assertions
                    .assertThat(PipelineExecutionUtils.round(actualResult))
                    .as("index: %s", index)
                    .isEqualTo(PipelineExecutionUtils.round(expectedResult));

            }
        }

    }

    /**
     * Perform the actual test for an <code>Operator</code> that handles multiple variables.
     *
     * @param pipeline        the {@link Pipeline} instance to execute.
     * @param testData        a list of time slices to pass to the {@code Pipeline}.
     * @param expectedResults the results expected from the {@code Pipeline}.
     */
    final static public void executeTestForMultipleVariable(Pipeline pipeline,
                                                            List<List<Double[]>> testData,
                                                            List<Double[]> expectedResults) {

        // Ensure there is at least one (1) set of test data.
        Assertions
            .assertThat(testData.size())
            .isNotZero();

        // Ensure time slice counts match.
        List<Double[]> firstVariableData = testData.get(0);
        int sliceCounts = firstVariableData.size();
        for (List<Double[]> variableData : testData) {
            Assertions
                .assertThat(variableData.size())
                .isEqualTo(sliceCounts);
        }

        // Ensure expected results count match.
        int variableDataLength = firstVariableData.get(0).length;
        for (Double[] expectedResult : expectedResults) {
            Assertions
                .assertThat(variableDataLength)
                .isEqualTo(expectedResult.length);
        }

        // Loop through each variables data.
        for (int index = 0; index < firstVariableData.size(); index++) {
            List<Double[]> variableDataWrapper = new ArrayList<>();
            for (List<Double[]> variableData : testData) {
                variableDataWrapper.add(variableData.get(index));
            }
            pipeline.execute(variableDataWrapper);
        }

        // Validate the results.
        List<Double[]> resultsList = pipeline.getResults();
        Assertions
            .assertThat(resultsList.size())
            .isEqualTo(expectedResults.size());
        for (int listIndex = 0; listIndex < resultsList.size(); listIndex++) {
            Double[] result = resultsList.get(listIndex);
            Double[] expectedResult = expectedResults.get(listIndex);
            Assertions
                .assertThat(result.length)
                .isEqualTo(expectedResult.length);

            for (int itemIndex = 0; itemIndex < result.length; itemIndex++) {

                // Remove precision from answers to avoid floating point errors and compare.
                Double expectedValue = PipelineExecutionUtils.round(expectedResult[itemIndex]);
                Double actualValue = PipelineExecutionUtils.round(result[itemIndex]);
                Assertions
                    .assertThat(actualValue)
                    .as("index: %s", itemIndex)
                    .isEqualTo(expectedValue);

            }
        }

    }

    /**
     * Perform the actual test for an <code>Operator</code> that handles multiple variables.
     *
     * @param pipeline        the {@link Pipeline} to execute.
     * @param testData        a list of time slices to pass to the {@code Pipeline}.
     * @param expectedResults the results expected from the {@code Pipeline}.
     */
    final static public void executeTestForMultipleVariable(Pipeline pipeline,
                                                            Map<Double, Double[]> testData,
                                                            List<Double[]> expectedResults) {

        // Loop through each time slice.
        List<List<Double[]>> mutatedTestData = new ArrayList<>();
        List<Double[]> variableDataWrapper = new ArrayList<Double[]>();
        mutatedTestData.add(variableDataWrapper);
        for (Double time : testData.keySet()) {
            Double[] data = testData.get(time);
            variableDataWrapper.add(data);
        }

        PipelineExecutionUtils.executeTestForMultipleVariable(pipeline, mutatedTestData,
            expectedResults);
    }

    /**
     * Utility method to round a <code>Double</code> to remove floating point calculation errors.
     */
    final static public Double round(Double value) {
        return Math.round(value * 100) / 100.0;
    }

}
