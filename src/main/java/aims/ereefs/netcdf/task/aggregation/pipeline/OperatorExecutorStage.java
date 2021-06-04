package aims.ereefs.netcdf.task.aggregation.pipeline;

/**
 * A {@code Pipeline} {@code Stage} to execute a single {@link PipelineContext#summaryOperator}.
 *
 * @author Aaron Smith
 */
public class OperatorExecutorStage extends BaseStage {

    /**
     * Cached reference to the next {@code Stage}. This next stage does not extend the
     * {@link BaseStage} class, so it must be specified explicitly here.
     */
    protected AccumulationStage accumulationStage;

    /**
     * Setter for the {@link #accumulationStage} property.
     *
     * @param accumulationStage a reference to the next {@code Stage} to cache.
     */
    public void setAccumulationStage(AccumulationStage accumulationStage) {
        this.accumulationStage = accumulationStage;
    }

    public void execute() {
        this.logger.debug(this.pipelineContext.getSummaryOperator().toString());
        this.accumulationStage.execute();
    }
}
