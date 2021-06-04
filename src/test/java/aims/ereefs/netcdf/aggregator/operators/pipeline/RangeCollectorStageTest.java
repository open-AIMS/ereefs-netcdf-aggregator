package aims.ereefs.netcdf.aggregator.operators.pipeline;

import aims.ereefs.netcdf.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link RangeCollectorStage} class.
 *
 * @author Aaron Smith
 */
public class RangeCollectorStageTest {

    @Test
    public void testInvalidInputArrayCount() {
        Assertions.assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> {
                RangeCollectorStage rangeCollectorStage = new RangeCollectorStage();
                rangeCollectorStage.execute(new ArrayList<Double[]>());
            })
            .withMessage(RangeCollectorStage.EXCEPTION_MESSAGE);
    }

    @Test
    public void testValid() {

        // Instantiate the target class.
        final RangeCollectorStage rangeCollectorStage = new RangeCollectorStage();

        // Execute with test data.
        rangeCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN, null, 1.0, 2.0
                    }
                );
            }
        });
        rangeCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        5.6, 0.3, 13.0, 2.01
                    }
                );
            }
        });
        rangeCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        15.6, 10.3, 3.0, 0.01
                    }
                );
            }
        });

        // Verify the results.
        final List<Double[]> results = rangeCollectorStage.getResults();
        Assertions
            .assertThat(results)
            .hasSize(3);
        final Double[] minResult = results.get(0);
        final Double[] maxResult = results.get(1);
        final Double[] rangeResult = results.get(2);

        TestUtils.assertSame(minResult, new Double[] {5.6, 0.3, 1.0, 0.01});
        TestUtils.assertSame(maxResult, new Double[] {15.6, 10.3, 13.0, 2.01});
        TestUtils.assertSame(rangeResult, new Double[] {10.0, 10.0, 12.0, 2.0});

    }
}
