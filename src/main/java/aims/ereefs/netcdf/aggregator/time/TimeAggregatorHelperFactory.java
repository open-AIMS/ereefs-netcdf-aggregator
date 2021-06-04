package aims.ereefs.netcdf.aggregator.time;


import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.config.SeasonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.units.DateUnit;

import java.util.List;

/**
 * Factory for obtaining the appropriate {@link TimeAggregatorHelper} instance.
 *
 * @author Aaron Smith
 */
public class TimeAggregatorHelperFactory {

    static final protected Logger logger = LoggerFactory.getLogger(TimeAggregatorHelperFactory.class);

    public static TimeAggregatorHelper make(AggregationPeriods type,
                                            List<SeasonConfig> seasonConfigs,
                                            DateUnit dateUnit) {
        if (logger.isTraceEnabled()) {
            logger.trace("aggregationPeriods: " + type.name());
        }
        switch (type) {
            case ALL:
                return new AllTimeAggregatorHelper(dateUnit);
            case DAILY:
                return new DailyTimeAggregatorHelper();
            case MONTHLY:
                return new MonthlyTimeAggregatorHelper(dateUnit);
            case SEASONAL:
                return new SeasonalTimeAggregatorHelper(dateUnit, seasonConfigs);
            case ANNUAL:
                return new AnnualTimeAggregatorHelper(dateUnit);
            case NONE:
            default:
                return new NoAggregationTimeAggregatorHelper();

        }
    }
}
