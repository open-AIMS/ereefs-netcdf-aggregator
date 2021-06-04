package aims.ereefs.netcdf.task.aggregation.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A base implementation of a {@code Stage} of a {@code Pipeline}.
 *
 * @author Aaron Smith
 */
abstract public class BaseStage {

    /**
     * Class-specific {@code logger}.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cached reference to the {@link PipelineContext} shared amongst the {@code Stages}.
     */
    protected PipelineContext pipelineContext;

    /**
     * Setter for the {@link #pipelineContext} property.
     *
     * @param context the reference to cache.
     * @return a reference to the class for convenience.
     */
    public BaseStage setPipelineContext(PipelineContext context) {
        this.pipelineContext = context;
        return this;
    }

    /**
     * Reference to the next {@code Stage} to execute.
     */
    protected BaseStage nextStage;

    /**
     * Setter for the {@link #nextStage} property.
     *
     * @param nextStage the reference to cache.
     * @return a reference to the class for convenience.
     */
    public BaseStage setNextStage(BaseStage nextStage) {
        this.nextStage = nextStage;
        return this;
    }

    /**
     * Method invoked to perform the processing of the {@code Stage}.
     */
    abstract public void execute();

}
