package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface to support
 * <code>Monthly</code> aggregations.
 *
 * @author Aaron Smith
 */
public class MonthlyTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    private final DateUnit dateUnit;

    public MonthlyTimeAggregatorHelper(DateUnit dateUnit) {
        super();
        this.dateUnit = dateUnit;
    }

    /**
     * <code>Monthly</code>-specific implementation of the method, converts the date to the first
     * day of the corresponding month.
     */
    public double aggregateTime(double time) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(dateUnit, time);
        LocalDateTime startOfMonth = localDateTime
            .with(TemporalAdjusters.firstDayOfMonth())
            .truncatedTo(ChronoUnit.DAYS);
        return NetcdfDateUtils.fromLocalDateTime(dateUnit, startOfMonth);
    }

    @Override
    public String getDescriptor() {
        return "Monthly";
    }

}
