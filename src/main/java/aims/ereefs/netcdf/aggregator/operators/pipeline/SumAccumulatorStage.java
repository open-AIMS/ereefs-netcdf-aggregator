package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@code intermediate} {@link Stage} to accumulate ({@code SUM}) input data for a specified
 * {@link #maxAccumulationCount count}. {@link #nextStages Next stages} are invoked via the
 * {@link #executeNextStages(List)} method.
 *
 * @author Aaron Smith
 */
public class SumAccumulatorStage extends AbstractIntermediateStage {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Cached value of the number of accumulations to perform before invoking the
     * {@link #nextStages}.
     */
    protected int maxAccumulationCount;

    /**
     * The number of accumulations performed since instantiation, or since the last invocation of
     * the {@link #nextStages}.
     */
    protected int accumulationCounter = 0;

    /**
     * Internal arrays for accumulating the data.
     */
    protected List<Double[]> cachedData = null;

    public SumAccumulatorStage(int maxAccumulationCount,
                               List<Stage> nextStages) {
        super(nextStages);
        this.maxAccumulationCount = maxAccumulationCount;
    }

    @Override
    public void reset() {
        this.cachedData = null;
        this.accumulationCounter = 0;
        super.reset();
    }

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        // Instantiate the accumulation data if not already done.
        if (this.cachedData == null) {
            this.cachedData = new ArrayList<>();
            for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
                final Double[] input = inputs.get(inputIndex);
                final int dataLength = input.length;
                final Double[] accumulationDataArray = new Double[dataLength];
                Arrays.fill(accumulationDataArray, Double.NaN);
                this.cachedData.add(accumulationDataArray);
            }
        }

        // Loop through each input array in the inputs list.
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            final Double[] input = inputs.get(inputIndex);
            final int dataLength = input.length;
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

        // Increment the accumulation counter.
        this.accumulationCounter++;

        // Has the accumulation counter reached it's target?
        if (this.accumulationCounter >= this.maxAccumulationCount) {

            // Invoke the next stages.
            this.executeNextStages(this.cachedData);

            // Reset.
            this.cachedData = null;
            this.accumulationCounter = 0;

        }
    }

}
