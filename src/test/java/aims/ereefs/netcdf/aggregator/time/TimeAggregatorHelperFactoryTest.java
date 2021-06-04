package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.config.SeasonConfig;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link TimeAggregatorHelperFactory} class.
 *
 * @author Aaron Smith
 */
public class TimeAggregatorHelperFactoryTest {

    /**
     * Test instantiation of each {@link TimeAggregatorHelper} implementation class.
     */
    @Test
    public void testFactory() {

        try {

            // Instantiate class for 100% code coverage.
            new TimeAggregatorHelperFactory();

            // Instantiate parameters.
            DateUnit dateUnit = new DateUnit("days since 1990-01-01 00:00:00 +10");

            // Test each factory scenario.
            List<SeasonConfig> emptySeasonConfigs = new ArrayList<>();
            Assertions
                .assertThat(
                    TimeAggregatorHelperFactory.make(
                        AggregationPeriods.NONE,
                        emptySeasonConfigs,
                        dateUnit
                    )
                )
                .isInstanceOf(NoAggregationTimeAggregatorHelper.class);
            Assertions
                .assertThat(
                    TimeAggregatorHelperFactory.make(
                        AggregationPeriods.DAILY,
                        emptySeasonConfigs,
                        dateUnit
                    )
                )
                .isInstanceOf(DailyTimeAggregatorHelper.class);
            Assertions
                .assertThat(
                    TimeAggregatorHelperFactory.make(
                        AggregationPeriods.MONTHLY,
                        emptySeasonConfigs,
                        dateUnit
                    )
                )
                .isInstanceOf(MonthlyTimeAggregatorHelper.class);
            Assertions
                .assertThat(
                    TimeAggregatorHelperFactory.make(
                        AggregationPeriods.SEASONAL,
                        emptySeasonConfigs,
                        dateUnit
                    )
                )
                .isInstanceOf(SeasonalTimeAggregatorHelper.class);
            Assertions
                .assertThat(
                    TimeAggregatorHelperFactory.make(
                        AggregationPeriods.ALL,
                        emptySeasonConfigs,
                        dateUnit
                    )
                )
                .isInstanceOf(AllTimeAggregatorHelper.class);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

}
