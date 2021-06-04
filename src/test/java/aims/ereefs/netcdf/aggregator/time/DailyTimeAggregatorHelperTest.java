package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.config.SeasonConfig;
import aims.ereefs.netcdf.util.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link DailyTimeAggregatorHelper} class.
 *
 * @author Aaron Smith
 */
public class DailyTimeAggregatorHelperTest {

    /**
     * Test the {@link TimeAggregatorHelper#buildAggregatedTimeMap(Double[], int)} and
     * {@link DailyTimeAggregatorHelper#aggregateTime(double)} methods.
     */
    @Test
    public void testClass() {

        try {

            // Instantiate the helper.
            List<SeasonConfig> emptySeasonConfigs = new ArrayList<>();
            DateUnit dateUnit = DateUtils.getDateUnit();
            TimeAggregatorHelper helper = TimeAggregatorHelperFactory.make(AggregationPeriods.DAILY,
                emptySeasonConfigs, dateUnit);

            // Build test data, which is a list of time periods over a 60 day period, starting on
            // 1-Dec-2016.
            Double[] timeIndexes = DateUtils.makeTestData();

            // Test limiting the building of the aggregatedTimeMap. Limiting to a single day of
            // time indexes, so only expecting a single aggregated time to be returned.
            Assertions
                .assertThat(helper.buildAggregatedTimeMap(timeIndexes, DateUtils.HOURS_PER_DAY).size())
                .isEqualTo(1);

            // Ask the helper to build a list of aggregated times.
            Map<Double, List<Double>> aggregatedTimeMap = helper.buildAggregatedTimeMap(timeIndexes,
                timeIndexes.length);

            // Evaluate the results.

            // Expected number of days worth of data.
            Assertions
                .assertThat(aggregatedTimeMap)
                .hasSize(DateUtils.DEFAULT_NUMBER_OF_DAYS);

            Iterator<Double> keys = aggregatedTimeMap.keySet().iterator();

            // Each key in map represents an entire day. It should therefore map to a list of times
            // equal to the number of hours in a day. The first time of the first day should be the
            // start date/time of the input time array. The first time of the second day should be
            // one day after the start date/time of the input time array.
            Double firstDayKey = keys.next();
            List<Double> firstDayList = aggregatedTimeMap.get(firstDayKey);
            Assertions
                .assertThat(firstDayList)
                .hasSize(DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(firstDayList.get(0))
                .isEqualTo(DateUtils.getStartTime());

            // Second day.
            Double secondDayKey = keys.next();
            List<Double> secondDayList = aggregatedTimeMap.get(secondDayKey);
            Assertions
                .assertThat(secondDayList)
                .hasSize(DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(secondDayList.get(0))
                .isEqualTo(DateUtils.getStartTime() + 1);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

}
