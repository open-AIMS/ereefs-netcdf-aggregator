package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.List;

/**
 * Public interface for a {@code terminal operation} in a multi-{@link Stage} {@code Operator}
 * {@code Pipeline}. This interface signifies that the implementing class will not automatically
 * invoke any further {@link Stage}s in a multi-{@code Stage} {@code Pipeline}.
 */
public interface Collector {

    /**
     * Retrieve the results. Invoking this method clears the results.
     */
    List<Double[]> getResults();

}
