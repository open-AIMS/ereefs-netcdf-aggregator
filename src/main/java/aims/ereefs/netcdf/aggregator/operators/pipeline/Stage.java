package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.List;

/**
 * Public interface for classes that participate in a multi-{@code Stage}
 * {@code Operator} {@code Pipeline}.
 */
public interface Stage {

    /**
     * Perform the processing required for the {@link Stage}. Note that an implementation may
     * transform the data from a multiple input arrays, invoking the next {@link Stage} with only a
     * single input array. For example, transforming two dimensional velocity vectors into a single
     * speed scalar.
     *
     * @param inputs the input data to be processed. The data for a single variable is presented as
     *               an array of {@code Double} values. Multiple variables are presented with one
     *               array of {@code Double} values per variable. In that case, the order of the
     *               variables may be specific to the concrete implementation.
     */
    void execute(List<Double[]> inputs);

    /**
     * Resets the {@link Stage} in preparation for a new aggregation period.
     */
    void reset();

}
