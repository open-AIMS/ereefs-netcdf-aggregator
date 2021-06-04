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
 * Tests for the {@link AnnualTimeAggregatorHelper} class.
 *
 * @author Aaron Smith
 */
public class AnnualTimeAggregatorHelperTest {

    /**
     * Test the {@link TimeAggregatorHelper#buildAggregatedTimeMap(Double[], int)} and
     * {@link AnnualTimeAggregatorHelper#aggregateTime(double)} methods.
     */
    @Test
    public void testClass() {

        try {

            // Instantiate the helper.
            List<SeasonConfig> emptySeasonConfigs = new ArrayList<>();
            DateUnit dateUnit = DateUtils.getDateUnit();
            TimeAggregatorHelper helper = TimeAggregatorHelperFactory.make(AggregationPeriods.ANNUAL,
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

            // Data falls into two (2) years.
            Assertions
                .assertThat(aggregatedTimeMap)
                .hasSize(2);

            Iterator<Double>  keys = aggregatedTimeMap.keySet().iterator();

            // Each key in map represents a different year. It should therefore map to a list of
            // times equal to the number of hours in that year for the test data. The first time of
            // the first year should be the start date/time of the input time array. The first time
            // of the second year should be the expected number of days after the start date/time
            // of the input time array.

            // December has 31 days.
            Double firstYearKey = keys.next();
            List<Double> firstYearList = aggregatedTimeMap.get(firstYearKey);
            Assertions
                .assertThat(firstYearList)
                .hasSize(DateUtils.NUMBER_OF_DAYS_IN_DECEMBER * DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(firstYearList.get(0))
                .isEqualTo(DateUtils.getStartTime());

            // January has 30 days.
            Double secondYearKey = keys.next();
            List<Double> secondYearList = aggregatedTimeMap.get(secondYearKey);
            Assertions
                .assertThat(secondYearList)
                .hasSize((DateUtils.DEFAULT_NUMBER_OF_DAYS - DateUtils.NUMBER_OF_DAYS_IN_DECEMBER) *
                    DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(secondYearList.get(0))
                .isEqualTo(DateUtils.getStartTime() + DateUtils.NUMBER_OF_DAYS_IN_DECEMBER);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

}
