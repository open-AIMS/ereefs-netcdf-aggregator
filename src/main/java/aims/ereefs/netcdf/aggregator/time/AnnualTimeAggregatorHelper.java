package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface to support
 * <code>Annual</code> aggregations.
 *
 * @author Aaron Smith
 */
public class AnnualTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    private final DateUnit dateUnit;

    public AnnualTimeAggregatorHelper(DateUnit dateUnit) {
        super();
        this.dateUnit = dateUnit;
    }

    /**
     * <code>Annual</code>-specific implementation of the method, converts the date to the first
     * day of the corresponding year.
     */
    public double aggregateTime(double d) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(dateUnit, d);
        LocalDateTime startOfYear = localDateTime
            .with(TemporalAdjusters.firstDayOfYear())
            .truncatedTo(ChronoUnit.DAYS);
        return NetcdfDateUtils.fromLocalDateTime(dateUnit, startOfYear);
    }

    @Override
    public String getDescriptor() {
        return "Annual";
    }

}
