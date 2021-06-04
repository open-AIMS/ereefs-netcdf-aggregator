package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs <code>Seasonal</code>
 * aggregations.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class SeasonalAggregator extends AbstractAggregator {

    public SeasonalAggregator(TimeAggregatorHelper timeAggregatorHelper) {
        super(timeAggregatorHelper);
    }

}
