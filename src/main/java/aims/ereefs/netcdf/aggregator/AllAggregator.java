package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs an aggregation of
 * <code>all</code> data.
 *
 * @author Aaron Smith
 */
public class AllAggregator extends AbstractAggregator {

    public AllAggregator(TimeAggregatorHelper timeAggregatorHelper) {
        super(timeAggregatorHelper);
    }

}
