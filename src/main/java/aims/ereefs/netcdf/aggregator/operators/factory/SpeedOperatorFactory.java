package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.*;

import java.util.ArrayList;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * transform vector speed variables to a magnitude and calculate the {@link MinCollectorStage Min},
 * {@link MaxCollectorStage Max} and {@link MeanCollectorStage Mean} for each cell.
 *
 * @author Aaron Smith
 * @see SpeedTransformerStage
 * @see MinCollectorStage
 * @see MeanCollectorStage
 * @see MaxCollectorStage
 * @see CombiningPipeline
 */
public class SpeedOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "SPEED";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
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
        return new CombiningPipeline(
            speedTransformerStage,
            new ArrayList<Collector>() {{
                add(minCollectorStage);
                add(meanCollectorStage);
                add(maxCollectorStage);
            }}
        );


    }

}
