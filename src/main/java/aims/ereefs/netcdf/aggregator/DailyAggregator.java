package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs <code>Daily</code>
 * aggregations.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class DailyAggregator extends AbstractAggregator {

    /**
     * Constructor to pass the reference for the {@link TimeAggregatorHelper} implementation to
     * the parent constructor.
     */
    public DailyAggregator(TimeAggregatorHelper timeAggregationHelper) {
        super(timeAggregationHelper);
    }

}
