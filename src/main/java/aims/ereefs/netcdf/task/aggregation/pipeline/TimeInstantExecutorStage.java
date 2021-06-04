package aims.ereefs.netcdf.task.aggregation.pipeline;

import au.gov.aims.ereefs.pojo.task.NcAggregateTask;


/**
 * A {@code Pipeline} {@code Stage} to execute a single {@link PipelineContext#timeInstant}.
 *
 * @author Aaron Smith
 */
public class TimeInstantExecutorStage extends BaseStage {

    public void execute() {
        final NcAggregateTask task = this.pipelineContext.getTask();
        final int timeInstantIndex = this.pipelineContext.getTimeInstantIndex();
        final double aggregateTime = this.pipelineContext.getTimeInstant().getValue();
        this.logger.debug("aggregateTime: " + aggregateTime + " (" + (timeInstantIndex + 1) +
            " of " + task.getTimeInstants().size() + ")");
        this.nextStage.execute();
    }

}
