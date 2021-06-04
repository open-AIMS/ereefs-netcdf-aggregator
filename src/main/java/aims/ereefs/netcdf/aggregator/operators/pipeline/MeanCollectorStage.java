package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the {@link SumCollectorStage} to convert the result to a {@code MEAN} when
 * {@link #getResults()} is invoked.
 *
 * @author Aaron Smith
 */
public class MeanCollectorStage extends SumCollectorStage {

    /**
     * The number of accumulations performed since accumulating data.
     */
    protected int accumulationCounter = 0;

    @Override
    public void reset() {
        super.reset();
        this.accumulationCounter = 0;
    }

    /**
     * Increments {@link #accumulationCounter} on each execution.
     */
    @Override
    public void execute(List<Double[]> inputs) {
        super.execute(inputs);

        // Increment the accumulation counter.
        this.accumulationCounter++;

    }

    /**
     * Converts {@code SUM} to {@code MEAN}.
     */
    @Override
    public List<Double[]> getResults() {
        final List<Double[]> results = new ArrayList<>();
        for (Double[] cachedArray : super.getResults()) {
            final Double[] resultArray = new Double[cachedArray.length];
            for (int index = 0; index < cachedArray.length; index++) {
                resultArray[index] = cachedArray[index] / this.accumulationCounter;
            }
            results.add(resultArray);
        }

        return results;
    }

}
