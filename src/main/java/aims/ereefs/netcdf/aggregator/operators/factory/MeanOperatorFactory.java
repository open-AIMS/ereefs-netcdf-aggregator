package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.MeanCollectorStage;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * calculate the {@code Mean} value for each cell.
 *
 * @author Aaron Smith
 * @see MeanCollectorStage
 */
public class MeanOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "MEAN";

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
        return new BasicPipeline(
            meanCollectorStage,
            meanCollectorStage
        );
    }

}
