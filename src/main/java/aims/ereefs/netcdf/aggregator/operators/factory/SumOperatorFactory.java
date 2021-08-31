package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.SumCollectorStage;

/**
 * Concrete implementation of the {@link PipelineFactory} for instantiating a {@link Pipeline} to
 * calculate the {@code Sum} value for each cell.
 *
 * @author Aaron Smith
 * @see SumCollectorStage
 */
public class SumOperatorFactory extends AbstractOperatorFactory {

    /**
     * Constant identifying the name of the {@code Operator} supported by this {@code factory}.
     */
    public static final String OPERATOR_TYPE = "SUM";

    @Override
    public boolean supports(String operatorType) {
        return OPERATOR_TYPE.equalsIgnoreCase(operatorType);
    }

    /**
     * Factory method for instantiating the {@link Pipeline} class. This method assumes that if
     * multiple input variables are defined but only one (1) output variable is defined, then the
     * intention is to sum all input variables together, "reducing" from multiple inputs to
     * single output.
     */
    @Override
    public Pipeline make() {

        // Determine if the definition involves multiple input variables but only one (1) output
        // variable.
        boolean isReduced = false;
        if (this.summaryOperator.getInputVariables().size() > 1
                && this.summaryOperator.getOutputVariables().size() == 1) {
            isReduced = true;
        }
        final SumCollectorStage sumCollectorStage = new SumCollectorStage(isReduced);
        return new BasicPipeline(
            sumCollectorStage,
            sumCollectorStage
        );
    }

}
