package aims.ereefs.netcdf.aggregator.operators.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An abstract {@code Intermediate} {@link Stage} to conveniently execute following
 * {@link #nextStages stages}.
 *
 * @author Aaron Smith
 */
abstract public class AbstractIntermediateStage implements Stage {

    /**
     * Cached reference to a list of {@link Stage} implementations to invoke sequentially once
     * processing has completed.
     */
    protected List<Stage> nextStages;

    /**
     * Constructor to cache the references to the {@link #nextStages}.
     */
    public AbstractIntermediateStage(List<Stage> nextStages) {
        this.nextStages = nextStages;
    }

    /**
     * Helper method to execute the {@link #nextStages} with the data specified.
     */
    protected void executeNextStages(List<Double[]> data) {
        if (this.nextStages != null) {
            for (Stage nextStage : this.nextStages) {
                nextStage.execute(data);
            }
        }
    }

    /**
     * Helpful implementation of the {@link Stage#reset()} method that invokes {@code reset()} on
     * the next {@code Stages}.
     */
    @Override
    public void reset() {
        if (this.nextStages != null) {
            for (Stage nextStage : this.nextStages) {
                nextStage.reset();
            }
        }
    }

}
