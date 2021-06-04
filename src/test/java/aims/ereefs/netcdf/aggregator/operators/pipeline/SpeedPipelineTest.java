package aims.ereefs.netcdf.aggregator.operators.pipeline;

import aims.ereefs.netcdf.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link SpeedTransformerStage} class.
 *
 * @author Aaron Smith
 */
public class SpeedPipelineTest {

    /**
     * Execute the {@link SpeedTransformerStage} to ensure correct results.
     */
    @Test
    public void testValid() {

        // Instantiate the target class.
        final MinCollectorStage minCollectorStage = new MinCollectorStage();
        final MeanCollectorStage meanCollectorStage = new MeanCollectorStage();
        final MaxCollectorStage maxCollectorStage = new MaxCollectorStage();
        final SpeedTransformerStage speedTransformerStage = new SpeedTransformerStage(
            new ArrayList<Stage>() {{
                add(minCollectorStage);
                add(meanCollectorStage);
                add(maxCollectorStage);
            }}
        );

        // Execute with test data.
        speedTransformerStage.execute(new ArrayList<Double[]>() {
            {
                // U test data.
                add(
                    new Double[]{
                        Double.NaN, -1.0, -1.0, 1.0, null, 0.5, 0.5, -0.5
                    }
                );

                // V test data.
                add(
                    new Double[]{
                        Double.NaN, -1.0, -1.0, 1.0, 0.1, 2.5, 2.5, -2.5
                    }
                );
            }
        });
        speedTransformerStage.execute(new ArrayList<Double[]>() {
            {
                // U test data.
                add(
                    new Double[]{
                        Double.NaN, -0.1, null, 0.1, 1.0, 1.0, null, 1.0
                    }
                );

                // V test data.
                add(
                    new Double[]{
                        Double.NaN, -3.1, null, 3.1, 2.0, 2.0, null, 2.0
                    }
                );
            }
        });
        speedTransformerStage.execute(new ArrayList<Double[]>() {
            {
                // U test data.
                add(
                    new Double[]{
                        Double.NaN, 0.0, 0.3, null, 0.4, 1.0, 0.0, null
                    }
                );

                // V test data.
                add(
                    new Double[]{
                        Double.NaN, 1.0, 1.3, null, 2.4, 2.0, 2.0, null
                    }
                );
            }
        });
        speedTransformerStage.execute(new ArrayList<Double[]>() {
            {
                // U test data.
                add(
                    new Double[]{
                        Double.NaN, null, 0.5, 0.5, 0.4, 0.1, -1.1, 0.6
                    }
                );

                // V test data.
                add(
                    new Double[]{
                        Double.NaN, null, 3.5, 3.5, 2.4, null, -2.1, 2.6
                    }
                );
            }
        });

        // Verify the results.
        final List<Double[]> minResults = minCollectorStage.getResults();
        final List<Double[]> expectedMinResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN, 1.0, 1.33, 1.41, 2.24, 2.24, 2.0, 2.24
                    }
                );
            }
        };
        TestUtils.assertSame(minResults, expectedMinResults);

        final List<Double[]> meanResults = meanCollectorStage.getResults();
        final List<Double[]> expectedMeanResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN, 1.38, 1.57, 2.01, 1.78, 1.76, 1.73, 1.86
                    }
                );
            }
        };
        TestUtils.assertSame(meanResults, expectedMeanResults);

        final List<Double[]> maxResults = maxCollectorStage.getResults();
        final List<Double[]> expectedMaxResults = new ArrayList<Double[]>() {
            {
                add(
                    new Double[]{
                        Double.NaN, 3.10, 3.54, 3.54, 2.43, 2.55, 2.55, 2.67
                    }
                );
            }
        };
        TestUtils.assertSame(maxResults, expectedMaxResults);

    }

}
