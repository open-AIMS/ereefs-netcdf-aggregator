package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.MaxCollectorStage;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * calculate the {@code Max} value for each cell.
 *
 * @author Aaron Smith
 * @see MaxCollectorStage
 */
public class MaxOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "MAX";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
        final MaxCollectorStage maxCollectorStage = new MaxCollectorStage();
        return new BasicPipeline(
            maxCollectorStage,
            maxCollectorStage
        );
    }

}
