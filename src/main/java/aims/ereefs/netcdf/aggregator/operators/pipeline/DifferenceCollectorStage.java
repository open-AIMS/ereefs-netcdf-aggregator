package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
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

    final static protected String EXCEPTION_MESSAGE_NO_INPUT = "No input data specified.";

    final static protected String EXCEPTION_MESSAGE_RESULT_SIZE = "Result set must have the same size for minuends and subtrahends.";
    final static protected String EXCEPTION_MESSAGE_ARRAY_LENGTH = "Both result arrays need to have the same length to calculate the difference.";

    /**
     * Internal arrays for accumulating the data.
     */
    protected List<Double[]> cachedData = null;

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE_NO_INPUT);
        }

        // Instantiate the accumulation data if not already done.
        if (this.cachedData == null) {
            this.cachedData = new ArrayList<>();
        }

        this.cachedData.addAll(inputs);
    }

    @Override
    public List<Double[]> getResults() {

        // Validate inputs.
        if (this.cachedData.size() % 2 != 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE_RESULT_SIZE);
        }

        int arraySplitSize = this.cachedData.size() / 2;
        List<Double[]> minuendArrayList = this.cachedData.subList(0, arraySplitSize);
        List<Double[]> subtrahendArrayList = this.cachedData.subList(arraySplitSize, this.cachedData.size());

        if (minuendArrayList.get(0).length != subtrahendArrayList.get(0).length) {
            throw new RuntimeException(EXCEPTION_MESSAGE_ARRAY_LENGTH);
        }

        final List<Double[]> difference = new ArrayList<>();
        for (int i = 0; i < arraySplitSize; i++) {
            final Double[] minuendArray = minuendArrayList.get(i);
            final Double[] subtrahendArray = subtrahendArrayList.get(i);
            final Double[] results = new Double[minuendArray.length];

            for (int j = 0; j < minuendArray.length; j++) {
                if (this.isNumber(minuendArray[j]) && this.isNumber(subtrahendArray[j])) {
                    results[j] = minuendArray[j] - subtrahendArray[j];
                } else {
                    results[j] = Double.NaN;
                }
            }
            difference.add(results);
        }

        // calculate mean if multiple file input bounds
        final List<Double[]> results = new ArrayList<>();
        if (difference.size() > 1) {
            // get first array to add other array values to it
            final Double[] resultArray = difference.get(0);

            // iterate over remaining arrays in difference list (=> values for each input file bound)
            for (int i = 1; i < arraySplitSize; i++) {
                Double[] differenceArray = difference.get(i);
                for (int dataIndex = 0; dataIndex < differenceArray.length; dataIndex++) {
                    final Double inputValue = differenceArray[dataIndex];

                    // Sum up all values. Only process if the input is a valid number.
                    if ((inputValue != null) && !Double.isNaN(inputValue)) {
                        // Convert the cached value to 0.0 if required before adding the input value.
                        if (!Double.isNaN(resultArray[dataIndex])) {
                            resultArray[dataIndex] += inputValue;
                        } else {
                            resultArray[dataIndex] = inputValue;
                        }
                    }

                    // is this differenceArray the last in the list and the value in the resultArray is not a NaN,
                    // then calculate the mean
                    if (i+1 == arraySplitSize && !Double.isNaN(resultArray[dataIndex])) {
                        resultArray[dataIndex] = resultArray[dataIndex] / arraySplitSize;
                    }
                }
            }

            results.add(resultArray);
            
        } else {
            results.add(difference.get(0));
        }

        return results;
    }

    /**
     * Check if the value is not null and not NaN
     *
     * @param value The value to check
     * @return True if it is a number, false if not
     */
    private boolean isNumber(Double value) {
        return (value != null) && !Double.isNaN(value);
    }

    @Override
    public void reset() {
        // Drop the cached data so that it will be re-initialised the next time execute() is invoked.
        this.cachedData = null;
    }
}
