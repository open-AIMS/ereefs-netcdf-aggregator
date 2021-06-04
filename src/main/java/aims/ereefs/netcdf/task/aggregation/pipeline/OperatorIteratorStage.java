package aims.ereefs.netcdf.task.aggregation.pipeline;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A {@code Pipeline} {@code Stage} to iterate over the {@code SummaryOperators} defined in the
 * {@code ProductDefinition}. This {@code Stage} executes each subsequent {@code Stage} once for
 * each {@code SummaryOperator} by setting {@link PipelineContext#summaryOperator}.
 *
 * @author Aaron Smith
 */
public class OperatorIteratorStage extends BaseStage {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cached reference to the list of {@code SummaryOperator}s to iterate over.
     */
    protected List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorList;

    /**
     * Constructor to cache references to static system objects/components.
     */
    public OperatorIteratorStage(List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorList) {
        this.summaryOperatorList = summaryOperatorList;
    }

    /**
     * Iterate through the {@link #summaryOperatorList}, invoking subsequent {@code Stages} (see
     * {@link #nextStage}) for each {@code SummaryOperator} and setting
     * {@link PipelineContext#summaryOperator} accordingly to specify the operational context.
     */
    public void execute() {
        final int summaryOperatorCount = this.summaryOperatorList.size();
        for (int index = 0; index < summaryOperatorCount; index++) {
            final NcAggregateProductDefinition.SummaryOperator summaryOperator =
                this.summaryOperatorList.get(index);
            logger.debug("operator " + (index + 1) + " of " + summaryOperatorCount);
            this.pipelineContext.setSummaryOperator(summaryOperator);
            this.nextStage.execute();
        }
        ;
    }

}
