package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface to support
 * aggregation of <code>all</code> data.
 *
 * @author Aaron Smith
 */
public class AllTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    private final DateUnit dateUnit;

    /**
     * Cache of the value returned by {@link #aggregateTime(double)} so it is only calculated once.
     */
    private Double cachedAggregateTime = null;

    public AllTimeAggregatorHelper(DateUnit dateUnit) {
        this.dateUnit = dateUnit;
    }

    /**
     * When aggregating all data, a single <code>AggregatedTime</code> must be returned for all
     * date/time values. This is chosen to be the start of the eReefs model files, 1-Sept-2010.
     * This value is cached in {@link #cachedAggregateTime} to eliminate unnecessary re-calculation.
     */
    public double aggregateTime(double d) {
        if (this.cachedAggregateTime == null) {
            this.cachedAggregateTime = NetcdfDateUtils.fromLocalDateTime(
                this.dateUnit,
                LocalDateTime.of(2010, 9, 1, 0, 0)
            );
        }
        return this.cachedAggregateTime;
    }

    @Override
    public String getDescriptor() {
        return "All";
    }

}
