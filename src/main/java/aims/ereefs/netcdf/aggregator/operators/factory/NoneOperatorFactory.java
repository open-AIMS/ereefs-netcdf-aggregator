package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.CachingCollectorStage;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} that
 * performs no calculations on the data. Use of this {@link Pipeline} only makes sense when the
 * system is NOT performing an aggregation.
 *
 * @author Aaron Smith
 * @see CachingCollectorStage
 */
public class NoneOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "NONE";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class.
     */
    @Override
    public Pipeline make() {
        final CachingCollectorStage cachingCollectorStage = new CachingCollectorStage();
        return new BasicPipeline(
            cachingCollectorStage,
            cachingCollectorStage
        );
    }

}
