package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.RangeCollectorStage;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * calculate the {@code Range} value for each cell.
 *
 * @author Aaron Smith
 * @see RangeCollectorStage
 */
public class RangeOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "RANGE";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
        final RangeCollectorStage rangeCollectorStage = new RangeCollectorStage();
        return new BasicPipeline(
            rangeCollectorStage,
            rangeCollectorStage
        );
    }

}
