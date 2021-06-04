package aims.ereefs.netcdf.task.aggregation.pipeline;

import au.gov.aims.ereefs.pojo.task.NcAggregateTask;

import java.util.List;


/**
 * A {@code Pipeline} {@code Stage} to iterate over the {@code TimeInstants} defined in the
 * {@code Task}. This {@code Stage} executes each subsequent {@code Stage} once for each
 * {@code TimeInstant} by setting {@link PipelineContext#timeInstant} and
 * {@link PipelineContext#timeInstantIndex}.
 *
 * @author Aaron Smith
 */
public class TimeInstantIteratorStage extends BaseStage {

    /**
     * Iterate through the {@code TimeInstants} specified in {@link PipelineContext#task}, invoking
     * subsequent {@code Stages} (see {@link #nextStage}) for each {@code TimeInstant} and setting
     * {@link PipelineContext#timeInstant} and {@link PipelineContext#timeInstantIndex} accordingly
     * to specify the operational context.
     */
    public void execute() {
        final List<NcAggregateTask.TimeInstant> timeInstants = pipelineContext.getTask().getTimeInstants();
        for (int timeInstantIndex = 0; timeInstantIndex < timeInstants.size(); timeInstantIndex++) {
            this.pipelineContext.setTimeInstantIndex(timeInstantIndex);
            this.pipelineContext.setTimeInstant(timeInstants.get(timeInstantIndex));
            this.nextStage.execute();
        }
    }

}
