package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * An abstract base {@link Collector} {@link Stage} that caches values that pass the
 * {@link #comparator} test. Since this is a {@link Collector}, this is a {@code Terminal}
 * {@code stage}.
 *
 * @author Aaron Smith
 */
public class AbstractComparisonCollectorStage implements Stage, Collector {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Cached data updated when the {@link #comparator} test is passed.
     */
    protected List<Double[]> cachedData;

    /**
     * Cached comparator function used by {@link #execute(List)} to determine if the value should
     * be {@link #cachedData cached}.
     *
     * @see Comparators#GREATER_THAN_COMPARATOR
     * @see Comparators#LESS_THAN_COMPARATOR
     */
    protected BiPredicate<Double, Double> comparator;

    public AbstractComparisonCollectorStage(BiPredicate<Double, Double> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        // Instantiate the internal arrays if not already done.
        if (this.cachedData == null) {
            this.cachedData = new ArrayList<>();
            for (final Double[] input : inputs) {
                final int inputLength = input.length;
                final Double[] cacheArray = new Double[inputLength];
                this.cachedData.add(cacheArray);
                Arrays.fill(cacheArray, Double.NaN);
            }
        }

        // Test each cached value against the corresponding input value..
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            final Double[] inputArray = inputs.get(inputIndex);
            final Double[] cachedArray = this.cachedData.get(inputIndex);
            for (int dataIndex = 0; dataIndex < inputArray.length; dataIndex++) {
                final Double inputValue = inputArray[dataIndex];
                final Double cachedValue = cachedArray[dataIndex];

                // Only compare if the input value is a valid number.
                if ((inputValue != null) && !Double.isNaN(inputValue)) {

                    // Cache if the current value is not a number, or the input value passes the
                    // comparison test.
                    if ((cachedValue == null) || Double.isNaN(cachedValue) ||
                        this.comparator.test(inputValue, cachedValue)) {
                        cachedArray[dataIndex] = inputValue;
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        // Drop the cached data so it will be re-initialised the next time execute() is invoked.
        if (this.cachedData != null) {
            this.cachedData.clear();
            this.cachedData = null;
        }
    }

    @Override
    public List<Double[]> getResults() {
        return this.cachedData;
    }

}
