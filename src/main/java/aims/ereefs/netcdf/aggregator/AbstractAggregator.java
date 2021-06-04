package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import aims.ereefs.netcdf.aggregator.operators.pipeline.BasicPipeline;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;

import java.util.List;

/**
 * Abstract implementation of the <code>Aggregator</code> interface, this class delegates
 * aggregation to a {@link TimeAggregatorHelper} and a {@link BasicPipeline}.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public abstract class AbstractAggregator implements Aggregator {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The {@link BasicPipeline} that encapsulates the multi-{@code Stage} data processing pipeline
     * used for processing the data.
     */
    protected Pipeline pipeline = null;

    /**
     * The {@code Factory} class for instantiating and initialising the underlying
     * {@link #pipeline}.
     */
    protected PipelineFactory pipelineFactory;

    @Override
    public void setPipelineFactory(PipelineFactory factory) {
        this.pipelineFactory = factory;
    }

    /**
     * The shape of the input data. This value is used by the {@link #initialise()} method to
     * calculate the expected {@link #size} of the dataset.
     */
    private int[] shape;

    private int size;

    private DataType dataType;

    /**
     * Cached reference to the {@link TimeAggregatorHelper} helper class which is delegated the
     * work of calculating aggregation times.
     */
    protected TimeAggregatorHelper timeAggregatorHelper;

    /**
     * Constructor for capturing the reference to the {@link TimeAggregatorHelper} implementation
     * class.
     */
    protected AbstractAggregator(TimeAggregatorHelper timeAggregatorHelper) {
        super();
        this.timeAggregatorHelper = timeAggregatorHelper;
    }

    @Override
    public void initialise() {
        this.pipeline = this.pipelineFactory.make();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("operator: " + pipeline.getClass().getName());
        }
    }

    @Override
    public boolean isInitialised() {
        return this.pipeline != null;
    }

    @Override
    public void unInitialise() {
        this.pipeline = null;
    }

    @Override
    public void add(double time, List<Double[]> variableDataArrayList) {

        // Verify that an Operator has been specified.
        if (this.pipeline == null) {
            throw new RuntimeException("No operator specified.");
        }

        // Pass the variable data to the operator.
        this.pipeline.execute(variableDataArrayList);

    }

    @Override
    public List<Double[]> getAggregatedData() {
        return this.pipeline.getResults();
    }

    @Override
    public void reset() {
        if (this.pipeline != null) {
            this.pipeline.reset();
        }
    }

    @Override
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public void setShape(int[] shape) {
        this.shape = shape;
        this.size = 1;
        for (int i : shape) {
            this.size = this.size * i;
        }
    }

    @Override
    public int[] getShape() {
        return this.shape;
    }

    public double aggregateTime(double time) {
        return this.timeAggregatorHelper.aggregateTime(time);
    }

    /**
     * Returns a concatenation of {@link TimeAggregatorHelper#getDescriptor()} and
     * {@link PipelineFactory#getDescriptor()}.
     */
    @Override
    public String getDescriptor() {
        return this.timeAggregatorHelper.getDescriptor() + "_" + this.pipelineFactory.getDescriptor();
    }

}
