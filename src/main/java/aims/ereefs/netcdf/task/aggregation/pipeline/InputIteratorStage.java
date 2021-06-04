package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.util.Constants;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Pipeline} {@code Stage} to iterate over the {@code Inputs} of the current
 * {@link PipelineContext#timeInstant TimeInstant}.
 *
 * @author Aaron Smith
 */
public class InputIteratorStage extends BaseStage {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Iterate through the {@code Inputs} for the current
     * {@link PipelineContext#timeInstant TimeInstant}. Where the {@code Input} contains data for
     * the current {@link PipelineContext#summaryOperator}, invoke subsequent {@code Stages} (see
     * {@link #nextStage}) for the {@code Input}, setting
     * {@link PipelineContext#input} accordingly to specify the operational context.
     */
    public void execute() {
        final NcAggregateTask.TimeInstant timeInstant = pipelineContext.getTimeInstant();
        timeInstant.getInputs().forEach(input -> {

            // Identify the InputId for the SummaryOperator. This does not support the SummaryOperator
            // requiring data from more than one input source. Input sources must be combined in a
            // pre-processing stage for that scenario.
            final NcAggregateProductDefinition.SummaryOperator summaryOperator =
                pipelineContext.getSummaryOperator();
            final String referenceVariableName = summaryOperator.getInputVariables().get(0);
            final String[] refVariableNameTokens = referenceVariableName.split(
                Constants.VARIABLE_NAME_SEPARATOR
            );
            if (refVariableNameTokens.length != 2) {
                throw new RuntimeException("Variable \"" + referenceVariableName +
                    "\" not fully qualified.");
            }
            final String inputId = refVariableNameTokens[0];

            // Ensure this Input contains data for the Summary Operator.
            if (input.getInputId().equals(inputId)) {
                this.logger.debug("input: " + input.getInputId());
                this.pipelineContext.setInput(input);
                this.nextStage.execute();
            }
        });
    }

}
