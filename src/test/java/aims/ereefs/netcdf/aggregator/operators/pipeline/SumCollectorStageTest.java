package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link SumCollectorStage} class.
 *
 * @author Aaron Smith
 */
public class SumCollectorStageTest {

    /**
     * Execute the {@link SumCollectorStage} to ensure correct results.
     */
    @Test
    public void testValid() {

        // Test target class with isReduced = false
        SumCollectorStage sumCollectorStage = new SumCollectorStage(false);
        sumCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0});
                add(new Double[]{1.0, Double.NaN, 3.0, 4.0});
            }
        });
        sumCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0});
                add(new Double[]{1.0, Double.NaN, 3.0, 4.0});
            }
        });
        List<Double[]> results = sumCollectorStage.getResults();
        List<Double[]> expectedResults = new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN,2.0,4.0,6.0});
                add(new Double[]{2.0,Double.NaN,6.0,8.0});
            }
        };
        Assertions
            .assertThat(results)
            .containsExactlyElementsOf(expectedResults);

        // Test target class with isReduced = true
        sumCollectorStage = new SumCollectorStage(true);
        sumCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0});
                add(new Double[]{1.0, Double.NaN, 3.0, 4.0});
            }
        });
        sumCollectorStage.execute(new ArrayList<Double[]>() {
            {
                add(new Double[]{Double.NaN, 1.0, 2.0, 3.0});
                add(new Double[]{1.0, Double.NaN, 3.0, 4.0});
            }
        });
        results = sumCollectorStage.getResults();
        expectedResults = new ArrayList<Double[]>() {
            {
                add(new Double[]{2.0,2.0,10.0,14.0});
            }
        };
        Assertions
                .assertThat(results)
                .containsExactlyElementsOf(expectedResults);

    }
}
