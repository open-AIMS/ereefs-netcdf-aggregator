package aims.ereefs.netcdf.aggregator;

import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactory;
import ucar.ma2.DataType;

import java.util.List;

/**
 * Interface for classes that manage temporal aggregation functions.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public interface Aggregator {

    /**
     * Instructs the {@code Aggregator} to prepare any internal structures and properties prior to
     * receiving any data. This method would normally be called once for an {@code Aggregator}.
     */
    void initialise();

    /**
     * Allows the caller to check the initialisation status of the {@code Aggregator}.
     *
     * @see #initialise()
     */
    boolean isInitialised();

    /**
     * Instructs the {@code Aggregator} to clear any internal structures, returning to a state
     * requiring the {@link #initialise()} method to be invoked before use.
     */
    void unInitialise();

    /**
     * Add the data to the underlying {@code Operator}.
     *
     * @param time             the time corresponding to the variable data.
     * @param variableDataList a list of input arrays ({@code Double[]} where each array represents
     *                         a single time slice of data for a single variable.
     */
    void add(double time, List<Double[]> variableDataList);

    /**
     * Returns the results of the aggregation. Each array ({@code Double[]}) in the returned list
     * represents an output variable.
     *
     * @return one or more arrays ({@code Double[]}) of aggregated data.
     */
    List<Double[]> getAggregatedData();

    /**
     * Instruct the {@code Aggregator} that processing for an aggregation period has completed, and
     * the next aggregation period is about to start.
     */
    void reset();

    /**
     * Calculate the aggregated time for the specified time. This calculation should normally be
     * delegated to a {@link aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper} implementation
     * class.
     */
    double aggregateTime(double time);

    void setShape(int[] shape);

    /**
     * A {@code String} providing a description of the {@code Aggregator} for informational
     * purposes.
     */
    String getDescriptor();

    int[] getShape();

    void setPipelineFactory(PipelineFactory factory);

    void setDataType(DataType dataType);
}
