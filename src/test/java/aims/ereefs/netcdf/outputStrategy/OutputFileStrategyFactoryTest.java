package aims.ereefs.netcdf.outputStrategy;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link aims.ereefs.netcdf.outputStrategy.OutputFileStrategyFactory} class.
 *
 * @author Aaron Smith
 */
public class OutputFileStrategyFactoryTest {

    /**
     * Test instantiation of each {@link aims.ereefs.netcdf.outputStrategy.OutputFileStrategy}
     * enumeration.
     */
    @Test
    public void testFactory() {

        // Instantiate class for 100% code coverage.
        try {
            new OutputFileStrategyFactory();
        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

        Assertions
            .assertThat(OutputFileStrategyFactory.make("INPUT"))
            .isEqualTo(OutputFileStrategy.INPUT);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("input"))
            .isEqualTo(OutputFileStrategy.INPUT);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("DAILY"))
            .isEqualTo(OutputFileStrategy.DAILY);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("daily"))
            .isEqualTo(OutputFileStrategy.DAILY);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("ONE"))
            .isEqualTo(OutputFileStrategy.ONE);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("one"))
            .isEqualTo(OutputFileStrategy.ONE);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("WEEKLY"))
            .isEqualTo(OutputFileStrategy.WEEKLY);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("weekly"))
            .isEqualTo(OutputFileStrategy.WEEKLY);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("MONTHLY"))
            .isEqualTo(OutputFileStrategy.MONTHLY);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("monthly"))
            .isEqualTo(OutputFileStrategy.MONTHLY);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("ANNUAL"))
            .isEqualTo(OutputFileStrategy.ANNUAL);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("annual"))
            .isEqualTo(OutputFileStrategy.ANNUAL);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("SEASONAL"))
            .isEqualTo(OutputFileStrategy.SEASONAL);
        Assertions
            .assertThat(OutputFileStrategyFactory.make("seasonal"))
            .isEqualTo(OutputFileStrategy.SEASONAL);

        Assertions
            .assertThat(OutputFileStrategyFactory.make("unknown"))
            .isEqualTo(OutputFileStrategy.INPUT);
    }

}
