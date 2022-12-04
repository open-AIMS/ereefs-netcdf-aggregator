package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Collector} to accumulate ({@code DIFF}) cell values over multiple
 * {@link #execute(List) executions}.
 *
 * <p>
 * The same variable from two inputs are compared by creating the difference as a new variable in the output.
 * </p>
 *
 * <p>While this class implements the {@link Stage} interface allowing it to be part of a
 * multi-{@link Stage} {@code Pipeline}, implementation of the {@link Collector} interface marks
 * this class as a terminal operation, which means it will not invoke any further {@link Stage}
 * instances.
 * </p>
 *
 * @author Aaron Smith
 */
public class DifferenceCollectorStage implements Stage, Collector {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Internal arrays for accumulating the data.
     */
    protected List<Double[]> cachedData = null;
    
    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        // Instantiate the accumulation data if not already done.
        if (this.cachedData == null) {
            this.cachedData = new ArrayList<>();

            for (Double[] input : inputs) {
                final Double[] accumulationDataArray = new Double[input.length];
                Arrays.fill(accumulationDataArray, Double.NaN);
                this.cachedData.add(accumulationDataArray);
            }
        }

        // Loop through each input array in the inputs list.
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            final Double[] input = inputs.get(inputIndex);
            final int dataLength = input.length;

            // Maintain separation of input variables.
            final Double[] cachedDataArray = this.cachedData.get(inputIndex);

            // Loop though each data cell in the array.
            for (int dataIndex = 0; dataIndex < dataLength; dataIndex++) {
                final Double inputValue = input[dataIndex];

                // Only process if the input is a valid number.
                if ((inputValue != null) && !Double.isNaN(inputValue)) {

                    // Convert the cached value to 0.0 if required before adding the input value.
                    cachedDataArray[dataIndex] =
                        (
                            !Double.isNaN(cachedDataArray[dataIndex]) ?
                                cachedDataArray[dataIndex] :
                                0.0
                        ) + inputValue;
                }
            }
        }

    }

    @Override
    public void reset() {
        // Drop the cached data so that it will be re-initialised the next time execute() is invoked.
        this.cachedData = null;
    }

    @Override
    public List<Double[]> getResults() {
        return this.cachedData;
    }

}
