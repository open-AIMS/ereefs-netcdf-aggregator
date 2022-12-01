package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.util.Constants;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        final NcAggregateProductDefinition.SummaryOperator summaryOperator =
                pipelineContext.getSummaryOperator();

        // Check if the operator uses different inputs and if yes, set add them to the pipeline context
        List<String> distinctInputIds = summaryOperator.getInputVariables().stream()
                .map(inputVariableName -> {
                    String[] inputVariableNameTokens = inputVariableName.split(
                            Constants.VARIABLE_NAME_SEPARATOR
                    );
                    if (inputVariableNameTokens.length != 2) {
                        throw new RuntimeException("Variable \"" + inputVariableName +
                                "\" not fully qualified.");
                    }
                    
                    return inputVariableNameTokens[0];
                })
                .distinct()
                .collect(Collectors.toList());

        // Collect all inputs for this time instant
        List<NcAggregateTask.Input> inputs = new ArrayList<>();
        distinctInputIds.forEach(distinctInputId -> {
            timeInstant.getInputs().stream()
                    .filter(timeInstantInput -> timeInstantInput.getInputId().equalsIgnoreCase(distinctInputId))
                    .findFirst()
                    .ifPresent(inputs::add);
        });

        if (inputs.size() > 0) {
            this.logger.debug("Using multiple inputs as different inputIds present in operator.");
            this.pipelineContext.setInputs(inputs);
            this.nextStage.execute();
        }
    }

}
