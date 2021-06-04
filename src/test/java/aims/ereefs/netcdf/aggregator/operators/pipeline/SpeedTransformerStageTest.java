package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link SpeedTransformerStage} class.
 *
 * @author Aaron Smith
 */
public class SpeedTransformerStageTest {

    /**
     * Execute the {@link SpeedTransformerStage} to ensure correct results.
     */
    @Test
    public void testValid() {

        // Instantiate the target class.
        final CachingCollectorStage cachingCollectorStage = new CachingCollectorStage();
        final SpeedTransformerStage speedTransformerStage = new SpeedTransformerStage(
            new ArrayList<Stage>() {{
                add(cachingCollectorStage);
            }}
        );

        // Execute with test data.
        speedTransformerStage.execute(new ArrayList<Double[]>() {
            {
                // U test data.
                add(
                    new Double[]{
                        Double.NaN, -1.0, -1.0, 3.0
                    }
                );

                // V test data.
                add(
                    new Double[]{
                        -1.0, Double.NaN, -2.0, 4.0
                    }
                );
            }
        });

        // Verify the results.
        final List<Double[]> results = cachingCollectorStage.getResults();
        final List<Double[]> expectedResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN,
                        Double.NaN,
                        Math.sqrt(Math.pow(-1.0, 2) + Math.pow(-2.0, 2)),
                        Math.sqrt(Math.pow(3.0, 2) + Math.pow(-4.0, 2))
                    }
                );
            }
        };

        Assertions
            .assertThat(results)
            .containsExactlyElementsOf(expectedResults);

    }
}
