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
 * Tests for the {@link AllTimeAggregatorHelper} class.
 *
 * @author Aaron Smith
 */
public class AllTimeAggregatorHelperTest {

    /**
     * Test the {@link TimeAggregatorHelper#buildAggregatedTimeMap(Double[], int)} and
     * {@link AllTimeAggregatorHelper#aggregateTime(double)} methods.
     */
    @Test
    public void testClass() {

        try {

            // Instantiate the helper.
            List<SeasonConfig> emptySeasonConfigs = new ArrayList<>();
            DateUnit dateUnit = DateUtils.getDateUnit();
            TimeAggregatorHelper helper = TimeAggregatorHelperFactory.make(AggregationPeriods.ALL,
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

            // All input times should be aggregated to a single time.
            Assertions
                .assertThat(aggregatedTimeMap)
                .hasSize(1);

            Iterator<Double> keys = aggregatedTimeMap.keySet().iterator();
            Double key = keys.next();
            List<Double> valueList = aggregatedTimeMap.get(key);
            Assertions
                .assertThat(valueList)
                .isNotEmpty()
                .hasSize(timeIndexes.length);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

}
