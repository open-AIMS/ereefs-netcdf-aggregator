package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.*;

import java.util.ArrayList;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * compare a variable from two inputs for each cell.
 *
 * @author Marc Hammerton
 * @see SpeedTransformerStage
 * @see MinCollectorStage
 * @see CombiningPipeline
 */
public class DifferenceOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "DIFF";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
        final DifferenceCollectorStage differenceCollectorStage = new DifferenceCollectorStage();
        final SubtractTransformerStage subtractTransformerStage = new SubtractTransformerStage(
                new ArrayList<Stage>() {{
                    add(differenceCollectorStage);
                }}
        );
        return new CombiningPipeline(
                subtractTransformerStage,
                new ArrayList<Collector>() {{
                    add(differenceCollectorStage);
                }}
        );
    }
}
