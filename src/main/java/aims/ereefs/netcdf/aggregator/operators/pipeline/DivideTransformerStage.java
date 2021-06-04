package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Stage} that divides every cell by a static {@link #divisor}
 *
 * @author Aaron Smith
 */
public class DivideTransformerStage extends AbstractIntermediateStage {

    protected Double divisor;

    public DivideTransformerStage(Double divisor,
                                  List<Stage> nextStages) {
        super(nextStages);
        this.divisor = divisor;
    }

    @Override
    public void execute(List<Double[]> inputs) {
        final List<Double[]> results = new ArrayList<>();
        for (Double[] inputArray : inputs) {
            final int inputArrayLength = inputArray.length;
            final Double[] resultArray = new Double[inputArrayLength];
            results.add(resultArray);
            for (int index = 0; index < inputArrayLength; index++) {
                resultArray[index] = inputArray[index] / this.divisor;
            }
        }
        this.executeNextStages(results);
    }
}
