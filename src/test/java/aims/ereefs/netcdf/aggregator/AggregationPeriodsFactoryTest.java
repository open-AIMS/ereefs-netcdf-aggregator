package aims.ereefs.netcdf.aggregator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link AggregationPeriodsFactory} class.
 *
 * @author Aaron Smith
 */
public class AggregationPeriodsFactoryTest {

    /**
     * Test instantiation of each {@link AggregationPeriods}.
     */
    @Test
    public void testFactory() {

        try {

            // Instantiate class for 100% code coverage.
            new AggregationPeriodsFactory();

            // Test each factory scenario.
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.NONE.name()))
                .isEqualTo(AggregationPeriods.NONE);
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.DAILY.name()))
                .isEqualTo(AggregationPeriods.DAILY);
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.MONTHLY.name()))
                .isEqualTo(AggregationPeriods.MONTHLY);
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.SEASONAL.name()))
                .isEqualTo(AggregationPeriods.SEASONAL);
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.ANNUAL.name()))
                .isEqualTo(AggregationPeriods.ANNUAL);
            Assertions
                .assertThat(AggregationPeriodsFactory.make(AggregationPeriods.ALL.name()))
                .isEqualTo(AggregationPeriods.ALL);

        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

}
