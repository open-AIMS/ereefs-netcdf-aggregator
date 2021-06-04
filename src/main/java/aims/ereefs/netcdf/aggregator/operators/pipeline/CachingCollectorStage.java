package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Collector} {@link Stage} that {@link #cachedData caches} input data. On each
 * {@link #execute(List)} execution, the first array of input data is added to the
 * {@link #cachedData}.
 *
 * @author Aaron Smith
 */
public class CachingCollectorStage implements Stage, Collector {

    final static protected String EXCEPTION_MESSAGE = "No input data specified.";

    /**
     * Cache of the most recent input data.
     */
    protected List<Double[]> cachedData = new ArrayList<>();

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() == 0) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        // Cache the first array of input data.
        this.cachedData.add(inputs.get(0));

    }

    @Override
    public void reset() {
        this.cachedData.clear();
    }

    @Override
    public List<Double[]> getResults() {
        return this.cachedData;
    }

}
