package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.util.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link OutputFilenameGeneratorFactory} class.
 *
 * @author Aaron Smith
 */
public class OutputFilenameGeneratorFactoryTest {

    /**
     * Instantiate the <code>Factory</code> for 100% code coverage.
     */
    @Test
    public void testInstantiateFactory() {
        try {
            new OutputFilenameGeneratorFactory();
        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Test instantiating a {@link AnnualFilenameGenerator} filename generator.
     */
    @Test
    public void testAnnual() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
            OutputFileStrategy.ANNUAL,
            "PATTERN",
            DateUtils.getDateUnit()
        );
        Assertions
            .assertThat(generator)
            .isInstanceOf(AnnualFilenameGenerator.class);
    }

    /**
     * Test instantiating a {@link DailyFilenameGenerator} filename generator.
     */
    @Test
    public void testDaily() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
            OutputFileStrategy.DAILY,
            "PATTERN",
            DateUtils.getDateUnit()
        );
        Assertions
            .assertThat(generator)
            .isInstanceOf(DailyFilenameGenerator.class);
    }

    /**
     * Test instantiating a {@link JustOneFilenameGenerator} filename generator.
     */
    @Test
    public void testOne() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
            OutputFileStrategy.ONE,
            "PATTERN",
            DateUtils.getDateUnit()
        );
        Assertions
            .assertThat(generator)
            .isInstanceOf(JustOneFilenameGenerator.class);
    }

    /**
     * Test instantiating a {@link WeeklyFilenameGenerator} filename generator.
     */
    @Test
    public void testWeek() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
            OutputFileStrategy.WEEKLY,
            "PATTERN",
            DateUtils.getDateUnit()
        );
        Assertions
            .assertThat(generator)
            .isInstanceOf(WeeklyFilenameGenerator.class);
    }

    /**
     * Test instantiating a {@link AnnualFilenameGenerator} filename generator.
     */
    @Test
    public void testMonth() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
                    OutputFileStrategy.MONTHLY,
                    "PATTERN",
                    DateUtils.getDateUnit()
        );
        Assertions
            .assertThat(generator)
            .isInstanceOf(MonthlyFilenameGenerator.class);
    }

    /**
     * Test instantiating a {@link SeasonalFilenameGenerator} filename generator.
     */
    @Test
    public void testSeason() {
        try {
            OutputFilenameGeneratorFactory.make(
                OutputFileStrategy.SEASONAL,
                "PATTERN",
                DateUtils.getDateUnit()
            );
            Assertions.fail("Expected UnsupportedOperationException.");
        } catch(Exception e) {
            Assertions.assertThat(e).isInstanceOf(UnsupportedOperationException.class);
        }
    }

}
