package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link MaxCollectorStage} class.
 *
 * @author Aaron Smith
 */
public class MaxCollectorStageTest {

    @Test
    public void testInvalidInputArrayCount() {
        Assertions.assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> {
                MaxCollectorStage maxCollectorStage = new MaxCollectorStage();
                maxCollectorStage.execute(new ArrayList<Double[]>());
            })
            .withMessage(SumCollectorStage.EXCEPTION_MESSAGE);
    }

    @Test
    public void testValid() {

        // Instantiate the target class.
        final MaxCollectorStage maxCollectorStage = new MaxCollectorStage();

        // Execute with test data.
        maxCollectorStage.execute(new ArrayList<Double[]>() {
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
        maxCollectorStage.execute(new ArrayList<Double[]>() {
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
        maxCollectorStage.execute(new ArrayList<Double[]>() {
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
        final List<Double[]> results = maxCollectorStage.getResults();
        final List<Double[]> expectedResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        10.0, 3.1, 1.0, 3.2
                    }
                );
                add(
                    new Double[]{
                        15.6, 10.3, 23.0, 20.01
                    }
                );
            }
        };

        Assertions
            .assertThat(results)
            .containsExactlyElementsOf(expectedResults);

    }
}
