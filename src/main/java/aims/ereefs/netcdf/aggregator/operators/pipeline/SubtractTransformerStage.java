package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code intermediate} {@link Stage} to subtract the variable value from input B from the same variable in
 * input A. {@link #nextStages Next stages} are invoked via the {@link #executeNextStages(List)} method.
 *
 * @author Marc Hammerton
 */
public class SubtractTransformerStage extends AbstractIntermediateStage {

    final static protected String EXCEPTION_MESSAGE = "Subtract transformer expects 2 inputs with the same variables.";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public SubtractTransformerStage(List<Stage> nextStages) {
        super(nextStages);
    }

    @Override
    public void execute(List<Double[]> inputs) {

        // Validate inputs.
        if (inputs.size() != 2) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }

        logger.info("Subtracting values.");
        // Perform calculations.
        final Double[] aInputArray = inputs.get(0);
        final Double[] bInputArray = inputs.get(1);
        final int arrayLength = aInputArray.length;
        final Double[] difference = new Double[arrayLength];
        for (int index = 0; index < arrayLength; index++) {
            Double a = aInputArray[index];
            Double b = bInputArray[index];
            if ((a != null) && (!a.isNaN()) && (b != null) && (!b.isNaN())) {
                difference[index] = a - b;
            } else {
                difference[index] = Double.NaN;
            }
        }
        
        final List<Double[]> result = new ArrayList<>();
        result.add(difference);

        // Invoke next stages.
        this.executeNextStages(result);
        result.clear();

    }

}
