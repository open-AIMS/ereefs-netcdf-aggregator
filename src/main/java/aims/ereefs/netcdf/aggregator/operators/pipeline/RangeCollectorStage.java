package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite {@link Collector} that uses a {@link MinCollectorStage} and a
 * {@link MaxCollectorStage} to track the {@code minimum} and {@code maximum} values for each cell,
 * and then calculates the {@code Range} when the results are requested. Note that this class
 * supports only a single input variable (size of {@code inputs} is 1).
 *
 * <p>While this class implements the {@link Stage} interface allowing it to be part of a
 * multi-{@link Stage} {@code Pipeline}, implementation of the {@link Collector} interface marks
 * this class as a terminal operation, which means it will not invoke any further {@link Stage}
 * instances.
 * </p>
 *
 * @author Aaron Smith
 */
public class RangeCollectorStage implements Collector, Stage {

    final static protected String EXCEPTION_MESSAGE = "Only single variable input supported.";

    protected MinCollectorStage minCollectorStage = new MinCollectorStage();
    protected MaxCollectorStage maxCollectorStage = new MaxCollectorStage();

    @Override
    public void execute(List<Double[]> inputs) {
        if (inputs.size() != 1) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        this.minCollectorStage.execute(inputs);
        this.maxCollectorStage.execute(inputs);
    }

    @Override
    public void reset() {
        // Left intentionally blank as this class does not cache data.
    }

    @Override
    public List<Double[]> getResults() {
        final List<Double[]> minResults = this.minCollectorStage.getResults();
        if (minResults.size() != 1) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        final List<Double[]> maxResults = this.maxCollectorStage.getResults();
        if (maxResults.size() != 1) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        final Double[] minResult = minResults.get(0);
        final Double[] maxResult = maxResults.get(0);
        final Double[] rangeResult = new Double[minResult.length];
        for (int index = 0; index < minResult.length; index++) {
            final Double min = minResult[index];
            final Double max = maxResult[index];

            // If either value is null or NaN, make the range value NaN.
            if ((min != null) && !Double.isNaN(min) && (max != null) && !Double.isNaN(max)) {
                rangeResult[index] = Math.abs(max - min);
            } else {
                rangeResult[index] = Double.NaN;
            }
        }
        final List<Double[]> results = new ArrayList<>();
        results.add(minResult);
        results.add(maxResult);
        results.add(rangeResult);
        return results;
    }

}