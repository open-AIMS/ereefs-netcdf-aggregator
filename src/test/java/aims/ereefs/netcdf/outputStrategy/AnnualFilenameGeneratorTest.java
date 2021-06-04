package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.util.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link AnnualFilenameGenerator} class.
 *
 * @author Aaron Smith
 */
public class AnnualFilenameGeneratorTest {

    final int DEFAULT_HOURS_PER_TIME_INCREMENT = 1;

    final static public String PATTERN = OutputFilenameGeneratorHelper.PATTERN_PREFIX +
        "<year>" + OutputFilenameGeneratorHelper.PATTERN_POSTFIX;

    /**
     * Test the class.
     */
    @Test
    public void testGenerateForTime() {

        // Instantiate the FileNameGenerator.
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        // Format a known date.
        Assertions
            .assertThat(generator.generateForTime(DateUtils.getStartTime()))
            .startsWith(OutputFilenameGeneratorHelper.PATTERN_PREFIX)
            .contains("2016")
            .endsWith(OutputFilenameGeneratorHelper.PATTERN_POSTFIX);

    }

    @Test
    public void testCalculateExpectedTimeIndexes_AllAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThatThrownBy(() -> {
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.ALL,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            );
        }).hasMessage("Aggregation type \"ALL\" is incompatible with \"Annual\" files.");
    }

    @Test
    public void testCalculateExpectedTimeIndexes_AnnualAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThat(
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.ANNUAL,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            )
        ).isEqualTo(1);
    }

    @Test
    public void testCalculateExpectedTimeIndexes_DailyAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThat(
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.DAILY,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            )
        ).isEqualTo(366);
    }

    @Test
    public void testCalculateExpectedTimeIndexes_MonthlyAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThat(
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.MONTHLY,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            )
        ).isEqualTo(12);
    }

    @Test
    public void testCalculateExpectedTimeIndexes_NoneAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThat(
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.NONE,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            )
        ).isEqualTo(366 * 24);
    }

    @Test
    public void testCalculateExpectedTimeIndexes_SeasonalAggregationType() {
        OutputFileNameGenerator generator = OutputFilenameGeneratorHelper.makeGenerator(
            OutputFileStrategy.ANNUAL,
            PATTERN,
            AnnualFilenameGenerator.class
        );

        Assertions.assertThatThrownBy(() -> {
            generator.calculateExpectedTimeIndexes(
                DateUtils.getStartTime(),
                AggregationPeriods.SEASONAL,
                DEFAULT_HOURS_PER_TIME_INCREMENT
            );
        }).hasMessage("Aggregation type \"SEASONAL\" is incompatible with \"Annual\" files.");
    }

}
