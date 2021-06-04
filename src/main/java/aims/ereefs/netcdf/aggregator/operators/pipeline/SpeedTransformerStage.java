package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code intermediate} {@link Stage} to calculate speed from a two dimensional velocity
 * vector (u and v). {@link #nextStages Next stages} are invoked via the
 * {@link #executeNextStages(List)} method.
 *
 * @author Aaron Smith
 */
public class SpeedTransformerStage extends AbstractIntermediateStage {

    final static protected String EXCEPTION_MESSAGE = "Speed transformer expects 2 input variables: U and V";

    public SpeedTransformerStage(List<Stage> nextStages) {
        super(nextStages);
    }

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() != 2) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        // Perform calculations.
        final Double[] uArray = inputs.get(0);
        final Double[] vArray = inputs.get(1);
        final int arrayLength = uArray.length;
        final Double[] speed = new Double[arrayLength];
        for (int index = 0; index < arrayLength; index++) {
            Double u = uArray[index];
            Double v = vArray[index];
            if ((u != null) && (!u.isNaN()) && (v != null) && (!v.isNaN())) {
                speed[index] = Math.sqrt(Math.pow(u, 2) + Math.pow(v, 2));
            } else {
                speed[index] = Double.NaN;
            }
        }
        final List<Double[]> result = new ArrayList<>();
        result.add(speed);

        // Invoke next stages.
        this.executeNextStages(result);
        result.clear();

    }

}
