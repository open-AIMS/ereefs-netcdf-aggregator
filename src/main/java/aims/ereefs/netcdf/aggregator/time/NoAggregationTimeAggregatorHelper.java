package aims.ereefs.netcdf.aggregator.time;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface that does no aggregation
 * of input times.
 *
 * @author Aaron Smith
 */
public class NoAggregationTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    public double aggregateTime(double d) {
        return d;
    }

    @Override
    public String getDescriptor() {
        return "None";
    }

}
