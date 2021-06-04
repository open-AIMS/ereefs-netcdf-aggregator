package aims.ereefs.netcdf.aggregator.time;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface to support
 * <code>Daily</code> aggregations.
 *
 * @author Aaron Smith
 */
public class DailyTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    /**
     * <code>Daily</code>-specific implementation of the method, removes the fractional part of
     * the value, which represents the time component of the date/time, leaving only the date.
     */
    public double aggregateTime(double d) {
        return Math.floor(d);
    }

    @Override
    public String getDescriptor() {
        return "Daily";
    }

}
