package aims.ereefs.netcdf.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.stream.DoubleStream;

/**
 * Tests for the {@link WeightedMeanCalculator} class.
 *
 * @author Aaron Smith
 */
public class WeightedMeanCalculatorTest {

    /**
     * Test the {@link WeightedMeanCalculator} with valid data.
     */
    @Test
    public void testValid() {
        Double[] values = new Double[] { 5.0, 100.0, Double.NaN };
        Double[] weights = new Double[] { 1.0, 1.0/10.0, 5.0 };
        Assertions.assertThat(WeightedMeanCalculator.calculate(values, weights))
            .isEqualTo((values[0] * weights[0] + values[1] * weights[1])/(weights[0] + weights[1]));
    }

    /**
     * Test the {@link WeightedMeanCalculator} where the length of the {@code values} array is
     * different to the length of the {@code weights} array.
     */
    @Test
    public void testExceptionDifferentSizeInputs() {
        Double[] values = new Double[] { 5.0, 100.0 };
        Double[] weights = new Double[] { 1.0};
        Assertions.assertThatThrownBy(() -> {
            WeightedMeanCalculator.calculate(values, weights);
        }).isInstanceOf(RuntimeException.class);
    }

    /**
     * Test the {@link WeightedMeanCalculator} returns a NaN if no data was provided.
     */
    @Test
    public void testNoData() {
        Double[] values = new Double[] {};
        Double[] weights = new Double[] {};
        Assertions.assertThat(WeightedMeanCalculator.calculate(values, weights))
            .isNaN();
    }

}
