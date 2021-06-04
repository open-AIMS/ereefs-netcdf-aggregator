package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;
import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelperFactory;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;

/**
 * Factory for obtaining an {@link Aggregator} instance.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class AggregatorFactory {

    /**
     * Instantiate an {@link Aggregator} based on the parameters provided.
     */
    static public Aggregator make(AggregationPeriods aggregationPeriod,
                                  DateUnit dateUnit) {

        // TODO: Handle seasons.
        TimeAggregatorHelper timeAggregatorHelper = TimeAggregatorHelperFactory.make(
            aggregationPeriod,
            new ArrayList<>(),
            dateUnit
        );

        switch (aggregationPeriod) {
            case ALL:
                return new AllAggregator(timeAggregatorHelper);
            case DAILY:
                return new DailyAggregator(timeAggregatorHelper);
            case MONTHLY:
                return new MonthlyAggregator(timeAggregatorHelper);
            case SEASONAL:
                return new SeasonalAggregator(timeAggregatorHelper);
            case ANNUAL:
                return new AnnualAggregator(timeAggregatorHelper);
            case NONE:
            default:
                return new NoAggregationAggregator(timeAggregatorHelper);
        }
    }

}
