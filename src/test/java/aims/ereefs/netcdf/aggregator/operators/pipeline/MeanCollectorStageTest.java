package aims.ereefs.netcdf.aggregator.operators.pipeline;

import aims.ereefs.netcdf.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link MeanCollectorStage} class, which exercises most of the code in
 * {@link SumCollectorStage}.
 *
 * @author Aaron Smith
 */
public class MeanCollectorStageTest {

    @Test
    public void testInvalidInputArrayCount() {
        Assertions.assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> {
                MeanCollectorStage meanCollectorStage = new MeanCollectorStage();
                meanCollectorStage.execute(new ArrayList<Double[]>());
            })
            .withMessage(SumCollectorStage.EXCEPTION_MESSAGE);
    }

    @Test
    public void testValid() {

        // Instantiate the target class.
        final MeanCollectorStage meanCollectorStage = new MeanCollectorStage();

        // Execute with test data.
        meanCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN, null, 1.0, 2.0
                    }
                );
                add(
                    new Double[]{
                        5.6, 0.3, 13.0, 2.01
                    }
                );
            }
        });
        meanCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        4.3, 3.1, null, Double.NaN
                    }
                );
                add(
                    new Double[]{
                        15.6, 10.3, 3.0, 0.01
                    }
                );
            }
        });
        meanCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        10.0, 3.0, 0.1, 3.2
                    }
                );
                add(
                    new Double[]{
                        1.6, 1.3, 23.0, 20.01
                    }
                );
            }
        });

        // Verify the results.
        final List<Double[]> results = meanCollectorStage.getResults();
        final List<Double[]> expectedResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        (0.0 + 4.3 + 10.0) / 3,
                        (0.0 + 3.1 + 3.0) / 3,
                        (1.0 + 0.0 + 0.1) / 3,
                        (2.0 + 0.0 + 3.2) / 3
                    }
                );
                add(
                    new Double[]{
                        (5.6 + 15.6 + 1.6) / 3,
                        (0.3 + 10.3 + 1.3) / 3,
                        (13.0 + 3.0 + 23.0) / 3,
                        (2.01 + 0.01 + 20.01) / 3
                    }
                );
            }
        };

        TestUtils.assertSame(results, expectedResults);

    }
}
