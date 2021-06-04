package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.MinCollectorStage;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * calculate the {@code Min} value for each cell.
 *
 * @author Aaron Smith
 * @see MinCollectorStage
 */
public class MinOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "MIN";

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
        return new BasicPipeline(
            minCollectorStage,
            minCollectorStage
        );
    }

}
