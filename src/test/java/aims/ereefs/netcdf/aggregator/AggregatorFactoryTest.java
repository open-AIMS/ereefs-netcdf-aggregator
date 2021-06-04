package aims.ereefs.netcdf.aggregator;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.nc2.units.DateUnit;

/**
 * Tests for the {@link AggregatorFactory} class.
 *
 * @author Aaron Smith
 */
public class AggregatorFactoryTest {

    /**
     * Test instantiation of each {@link Aggregator} implementation class.
     */
    @Test
    public void testFactory() {

        try {

            // Instantiate parameters.
            DateUnit dateUnit = new DateUnit("days since 1990-01-01 00:00:00 +10");

            // Test each factory scenario.
            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.NONE, dateUnit)
                )
                .isInstanceOf(NoAggregationAggregator.class);

            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.DAILY, dateUnit)
                )
                .isInstanceOf(DailyAggregator.class);

            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.MONTHLY, dateUnit)
                )
                .isInstanceOf(MonthlyAggregator.class);

            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.SEASONAL, dateUnit)
                )
                .isInstanceOf(SeasonalAggregator.class);

            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.ANNUAL, dateUnit)
                )
                .isInstanceOf(AnnualAggregator.class);

            Assertions
                .assertThat(
                    AggregatorFactory.make(AggregationPeriods.ALL, dateUnit)
                )
                .isInstanceOf(AllAggregator.class);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

}
