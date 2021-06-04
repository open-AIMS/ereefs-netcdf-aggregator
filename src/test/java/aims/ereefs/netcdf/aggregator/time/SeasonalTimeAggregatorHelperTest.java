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
 * Tests for the {@link SeasonalTimeAggregatorHelper} class.
 *
 * @author Aaron Smith
 */
public class SeasonalTimeAggregatorHelperTest {

    /**
     * Test the {@link TimeAggregatorHelper#buildAggregatedTimeMap(Double[], int)} and
     * {@link SeasonalTimeAggregatorHelper#aggregateTime(double)} methods.
     */
    @Test
    public void testClass() {

        try {

            // Build a list of seasons.
            List<SeasonConfig> seasonConfigs = new ArrayList<>();
            seasonConfigs.add(SeasonConfig.make("firstSemester", "01-01"));
            seasonConfigs.add(SeasonConfig.make("secondSemester", "06-01"));

            // Instantiate the helper.
            DateUnit dateUnit = DateUtils.getDateUnit();
            TimeAggregatorHelper helper = TimeAggregatorHelperFactory.make(AggregationPeriods.SEASONAL,
                seasonConfigs, dateUnit);

            // Build test data, which is a list of time periods over a 60 day period, starting on
            // 1-Dec-2016.
            Double[] timeIndexes = DateUtils.makeTestData();

            // Ask the helper to build a list of aggregated times.
            Map<Double, List<Double>> aggregatedTimeMap = helper.buildAggregatedTimeMap(timeIndexes,
                timeIndexes.length);

            // Evaluate the results.

            // Two seasons worth of data.
            Assertions
                .assertThat(aggregatedTimeMap)
                .hasSize(2);

            Iterator<Double> keys = aggregatedTimeMap.keySet().iterator();

            // Each key in map represents a season, though each only contains a single month worth
            // of data. It should therefore map to a list of times equal to the number of hours in
            // that month. The first time of the first season should be the start date/time of the
            // input time array. The first time of the second season should be the expected number
            // of days after the start date/time of the input time array.

            // December has 31 days.
            Double decemberKey = keys.next();
            List<Double> decemberList = aggregatedTimeMap.get(decemberKey);
            Assertions
                .assertThat(decemberList)
                .hasSize(DateUtils.NUMBER_OF_DAYS_IN_DECEMBER * DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(decemberList.get(0))
                .isEqualTo(DateUtils.getStartTime());

            // January has 30 days.
            Double januaryKey = keys.next();
            List<Double> januaryList = aggregatedTimeMap.get(januaryKey);
            Assertions
                .assertThat(januaryList)
                .hasSize((DateUtils.DEFAULT_NUMBER_OF_DAYS - DateUtils.NUMBER_OF_DAYS_IN_DECEMBER) *
                    DateUtils.HOURS_PER_DAY);
            Assertions
                .assertThat(januaryList.get(0))
                .isEqualTo(DateUtils.getStartTime() + DateUtils.NUMBER_OF_DAYS_IN_DECEMBER);

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

}
