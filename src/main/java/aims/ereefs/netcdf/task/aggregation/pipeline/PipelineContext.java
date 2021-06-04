package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.output.summary.OutputWriter;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;

/**
 * Value object caching the current operational context of the {@code Pipeline}.
 */
public class PipelineContext {

    protected NcAggregateTask.Input input;

    protected NcAggregateProductDefinition productDefinition;

    protected NcAggregateProductDefinition.SummaryOperator summaryOperator;

    protected NcAggregateTask task;

    protected NcAggregateTask.TimeInstant timeInstant;

    protected int timeInstantIndex;

    public NcAggregateTask.Input getInput() {
        return this.input;
    }

    public NcAggregateProductDefinition getProductDefinition() {
        return this.productDefinition;
    }

    public NcAggregateProductDefinition.SummaryOperator getSummaryOperator() {
        return this.summaryOperator;
    }

    public NcAggregateTask getTask() {
        return this.task;
    }

    public NcAggregateTask.TimeInstant getTimeInstant() {
        return this.timeInstant;
    }

    public int getTimeInstantIndex() {
        return this.timeInstantIndex;
    }

    public void setInput(NcAggregateTask.Input input) {
        this.input = input;
    }

    public void setSummaryOperator(NcAggregateProductDefinition.SummaryOperator summaryOperator) {
        this.summaryOperator = summaryOperator;
    }

    public void setTimeInstant(NcAggregateTask.TimeInstant timeInstant) {
        this.timeInstant = timeInstant;
    }

    public void setTimeInstantIndex(int index) {
        this.timeInstantIndex = index;
    }

    /**
     * Flag indicating if an {@code OutputDataset} is being populated.
     */
    protected boolean isPopulatingOutputDataset;

    public boolean isPopulatingOutputDataset() {
        return this.isPopulatingOutputDataset;
    }

    /**
     * Cached reference to the {@link OutputWriter} for writing {@code SummaryStatistics}.
     */
    protected OutputWriter summaryOutputWriter;

    public OutputWriter getSummaryOutputWriter() {
        return this.summaryOutputWriter;
    }

    public PipelineContext(NcAggregateTask task,
                           NcAggregateProductDefinition productDefinition,
                           boolean isPopulatingOutputDataset,
                           OutputWriter summaryOutputWriter) {
        this.task = task;
        this.productDefinition = productDefinition;
        this.isPopulatingOutputDataset = isPopulatingOutputDataset;
        this.summaryOutputWriter = summaryOutputWriter;
    }
}
