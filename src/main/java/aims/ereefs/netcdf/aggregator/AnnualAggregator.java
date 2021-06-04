package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;

/**
 * Concrete implementation of the {@link Aggregator} interface that performs <code>Annual</code>
 * aggregations.
 *
 * @author Aaron Smith
 */
public class AnnualAggregator extends AbstractAggregator {

    public AnnualAggregator(TimeAggregatorHelper timeAggregatorHelper) {
        super(timeAggregatorHelper);
    }

}
