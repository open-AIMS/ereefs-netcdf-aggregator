package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.*;

import java.util.ArrayList;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * transform vector speed variables to a magnitude and calculate the {@link MeanCollectorStage Mean}
 * for each cell.
 *
 * @author Aaron Smith
 * @see SpeedTransformerStage
 * @see MinCollectorStage
 * @see MeanCollectorStage
 * @see MaxCollectorStage
 * @see CombiningPipeline
 */
public class SpeedMeanOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "SPEED_MEAN";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
        final MeanCollectorStage meanCollectorStage = new MeanCollectorStage();
        final SpeedTransformerStage speedTransformerStage = new SpeedTransformerStage(
            new ArrayList<Stage>() {{
                add(meanCollectorStage);
            }}
        );
        return new BasicPipeline(
            speedTransformerStage,
            meanCollectorStage
        );
    }

}
