package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs no actual aggregation.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class NoAggregationAggregator extends AbstractAggregator {

    /**
     * Constructor to pass the reference for the {@link TimeAggregatorHelper} implementation to
     * the parent constructor.
     */
    public NoAggregationAggregator(TimeAggregatorHelper timeAggregationHelper) {
        super(timeAggregationHelper);
    }

}
