package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.config.SeasonConfig;
import aims.ereefs.netcdf.util.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link NoAggregationTimeAggregatorHelper} class.
 *
 * @author Aaron Smith
 */
public class NoAggregationTimeAggregatorHelperTest {

    /**
     * Test the {@link TimeAggregatorHelper#buildAggregatedTimeMap(Double[], int)} and
     * {@link NoAggregationTimeAggregatorHelper#aggregateTime(double)} methods.
     */
    @Test
    public void testClass() {

        try {

            // Instantiate the helper.
            List<SeasonConfig> emptySeasonConfigs = new ArrayList<>();
            DateUnit dateUnit = DateUtils.getDateUnit();
            TimeAggregatorHelper helper = TimeAggregatorHelperFactory.make(AggregationPeriods.NONE,
                emptySeasonConfigs, dateUnit);

            // Build test data, which is a list of time periods over a 60 day period, starting on
            // 1-Dec-2016.
            Double[] timeIndexes = DateUtils.makeTestData();

            // Test limiting the building of the aggregatedTimeMap. No aggregation being performed,
            // so expect all hourly time indexes to be included for a single day.
            Assertions
                .assertThat(helper.buildAggregatedTimeMap(timeIndexes, DateUtils.HOURS_PER_DAY).size())
                .isEqualTo(24);

            // Ask the helper to build a list of aggregated times.
            Map<Double, List<Double>> aggregatedTimeMap = helper.buildAggregatedTimeMap(timeIndexes,
                timeIndexes.length);

            // Evaluate the results.

            // Expected number of aggregated times is equal to number of input times because every
            // input time is discreet (aggregated time == input time).
            Assertions
                .assertThat(aggregatedTimeMap)
                .hasSize(timeIndexes.length);
            for (int index = 0; index < timeIndexes.length; index++) {
                double inputTime = timeIndexes[index];
                List<Double> timeList = aggregatedTimeMap.get(inputTime);
                Assertions
                    .assertThat(timeList)
                    .hasSize(1);
                Assertions
                    .assertThat(timeList.get(0))
                    .isEqualTo(inputTime);
            }

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

}
