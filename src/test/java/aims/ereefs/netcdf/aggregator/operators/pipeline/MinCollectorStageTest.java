package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link MinCollectorStage} class.
 *
 * @author Aaron Smith
 */
public class MinCollectorStageTest {

    /**
     * Execute the {@link MinCollectorStage} to ensure correct results.
     */
    @Test
    public void testValid() {

        // Instantiate the target class.
        final MinCollectorStage minCollectorStage = new MinCollectorStage();

        // Execute with test data.
        minCollectorStage.execute(new ArrayList<Double[]>() {
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
        minCollectorStage.execute(new ArrayList<Double[]>() {
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
        minCollectorStage.execute(new ArrayList<Double[]>() {
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
        final List<Double[]> results = minCollectorStage.getResults();
        final List<Double[]> expectedResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        4.3, 3.0, 0.1, 2.0
                    }
                );
                add(
                    new Double[]{
                        1.6, 0.3, 3.0, 0.01
                    }
                );
            }
        };

        Assertions
            .assertThat(results)
            .containsExactlyElementsOf(expectedResults);

    }
}
