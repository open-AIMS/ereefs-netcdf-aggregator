package aims.ereefs.netcdf.output.summary;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link SummaryStatisticsCalculator} class.
 *
 * @author Aaron Smith
 */
public class SummaryStatisticsCalculatorTest {

    /**
     * Test list with zero entries.
     */
    @Test
    public void testEmpty() {
        Assertions
            .assertThat(SummaryStatisticsCalculator.calculate(new ArrayList<Double>()))
            .isNull();
    }

    /**
     * Test list with one (1) entry.
     */
    @Test
    public void testOneEntry() {
        List<Double> list = new ArrayList<Double>() {
            {
                add(1.0);
            }
        };

        SummaryStatistics statistics = SummaryStatisticsCalculator.calculate(list);
        Assertions.assertThat(statistics.getMean()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getMedian()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getLowPercentile()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getHighPercentile()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getLowest()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getHighest()).isEqualTo(1.0);
    }

    /**
     * Test list with two (2) entries.
     */
    @Test
    public void testTwoEntries() {
        List<Double> list = new ArrayList<Double>() {
            {
                add(1.0);
                add(2.0);
            }
        };

        SummaryStatistics statistics = SummaryStatisticsCalculator.calculate(list);
        Assertions.assertThat(statistics.getMean()).isEqualTo(1.5);
        Assertions.assertThat(statistics.getMedian()).isEqualTo(2.0); // Due to rounding.
        Assertions.assertThat(statistics.getLowPercentile()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getHighPercentile()).isEqualTo(2.0);
        Assertions.assertThat(statistics.getLowest()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getHighest()).isEqualTo(2.0);
    }

    /**
     * Test list with ten (10) entries.
     */
    @Test
    public void testTenEntries() {

        // Randomise entries of 0.0 to 9.0
        List<Double> list = new ArrayList<Double>() {
            {
                add(3.0);
                add(2.0);
                add(1.0);
                add(0.0);
                add(4.0);
                add(5.0);
                add(7.0);
                add(6.0);
                add(9.0);
                add(8.0);
            }
        };

        SummaryStatistics statistics = SummaryStatisticsCalculator.calculate(list);
        Assertions.assertThat(statistics.getMean()).isEqualTo(4.5);
        Assertions.assertThat(statistics.getMedian()).isEqualTo(5.0); // Due to rounding.
        Assertions.assertThat(statistics.getLowPercentile()).isEqualTo(1.0);
        Assertions.assertThat(statistics.getHighPercentile()).isEqualTo(9.0);  // Due to rounding.
        Assertions.assertThat(statistics.getLowest()).isEqualTo(0.0);
        Assertions.assertThat(statistics.getHighest()).isEqualTo(9.0);
    }

}
