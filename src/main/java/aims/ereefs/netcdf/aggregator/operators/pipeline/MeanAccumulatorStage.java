package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.List;

/**
 * A {@link Stage} implementation that extends {@link SumAccumulatorStage} to calculate the
 * {@code Mean} for each cell.
 *
 * @author Aaron Smith
 */
public class MeanAccumulatorStage extends SumAccumulatorStage {

    public MeanAccumulatorStage(int maxAccumulationCount,
                                List<Stage> nextStages) {
        super(maxAccumulationCount, nextStages);
    }

    /**
     * Override the inherited method to calculate the {@code Mean} of each cell before invoking
     * the inherited method.
     */
    @Override
    protected void executeNextStages(List<Double[]> data) {

        // Calculate the mean for every value.
        for (int arrayIndex = 0; arrayIndex < data.size(); arrayIndex++) {
            final Double[] array = data.get(arrayIndex);
            final int arrayLength = array.length;
            for (int dataIndex = 0; dataIndex < arrayLength; dataIndex++) {
                array[dataIndex] = array[dataIndex] / this.maxAccumulationCount;
            }
        }

        // Invoke the inherited method.
        super.executeNextStages(data);
    }

}
