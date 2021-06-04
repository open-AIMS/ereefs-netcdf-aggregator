package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs <code>Monthly</code>
 * aggregations.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class MonthlyAggregator extends AbstractAggregator {

    public MonthlyAggregator(TimeAggregatorHelper timeAggregatorHelper) {
        super(timeAggregatorHelper);
    }

}
