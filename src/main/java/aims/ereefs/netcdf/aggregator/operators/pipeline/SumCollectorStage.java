package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Collector} to accumulate ({@code SUM}) cell values over multiple
 * {@link #execute(List) executions}.
 *
 * <p>
 * The {@link #isReduced} flag determines the behaviour when this class is
 * {@link #execute(List) invoked} with multiple inputs. If {@code false}, then the number of output
 * arrays will match the number of input arrays. If {@code true} then each input array will be
 * added to a single {@link #cachedData} array, resulting in a single output array.
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
public class SumCollectorStage implements Stage, Collector {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Internal arrays for accumulating the data.
     */
    protected List<Double[]> cachedData = null;

    /**
     * Flag that determines if input arrays are reduced to a single output array (if {@code true}).
     */
    protected boolean isReduced = false;

    /**
     * Constructor allowing {@link #isReduced} to be set.
     */
    public SumCollectorStage(boolean isReduced) {
        super();
        this.isReduced = isReduced;
    }

    /**
     * Default constructor, invokes {@link #SumCollectorStage(boolean)} with {@code false}.
     */
    public SumCollectorStage() {
        this(false);
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

            // If isReduced is set, then reducing to a single output variable, otherwise maintain
            // separation of input variables. Create cache accordingly.
            if (this.isReduced) {
                final Double[] accumulationDataArray = new Double[inputs.get(0).length];
                Arrays.fill(accumulationDataArray, Double.NaN);
                this.cachedData.add(accumulationDataArray);
            } else {
                for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
                    final Double[] accumulationDataArray = new Double[inputs.get(inputIndex).length];
                    Arrays.fill(accumulationDataArray, Double.NaN);
                    this.cachedData.add(accumulationDataArray);
                }
            }
        }

        // Loop through each input array in the inputs list.
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            final Double[] input = inputs.get(inputIndex);
            final int dataLength = input.length;

            // If isReduced is set, then reducing to a single output variable, otherwise maintain
            // separation of input variables.
            final Double[] cachedDataArray = this.isReduced ?
                    this.cachedData.get(0) :
                    this.cachedData.get(inputIndex);

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
        // Drop the cached data so it will be re-initialised the next time execute() is invoked.
        this.cachedData = null;
    }

    @Override
    public List<Double[]> getResults() {
        return this.cachedData;
    }

}
