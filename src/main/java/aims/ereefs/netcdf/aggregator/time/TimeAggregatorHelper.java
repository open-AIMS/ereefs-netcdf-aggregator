package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.aggregator.Aggregator;

import java.util.List;
import java.util.Map;

/**
 * Interface for helper classes that perform aggregation of time values. For example, a concrete
 * implementation of this interface for <code>Monthly</code> aggregation would convert a single
 * <code>time</code> (eg: <code>9-Mar-2016</code>) to an aggregated time representing all values
 * within that month (eg: <code>1-Mar-2016</code>).
 *
 * @author Aaron Smith
 */
public interface TimeAggregatorHelper {

    /**
     * Calculate the aggregated time for each time value in the input array, grouping the results
     * by aggregated time in a map.
     *
     * @param timeIndexes            an array of time indexes.
     * @param maxExpectedTimeIndexes the maximum number of time indexes expected for the input file.
     *                               This value is used to limit the values used to generate the
     *                               aggregated times.
     * @return a <code>Map</code> where the <code>key</code> is a unique aggregated time, and the
     * value is a list of times from the input array that are grouped under the corresponding
     * aggregated time.
     */
    Map<Double, List<Double>> buildAggregatedTimeMap(Double[] timeIndexes,
                                                     int maxExpectedTimeIndexes);

    /**
     * Perform the aggregation of the specified time for the period of interest to the concrete
     * implementation of this interface. For example, an input value of <code>9-Mar-2016</code>
     * for a monthly aggregation would return an aggregated time of <code>1-Mar-2016</code>.
     * Similarly, any other date in Mar-2016 for a monthly aggregation would also return an
     * aggregated time of <code>1-Mar-2016</code>.
     *
     * @param time the time value to be converted to an aggregated time.
     * @return the aggregated time.
     */
    double aggregateTime(double time);

    /**
     * A descriptor of the temporal aggregation being supported by the implementation.
     *
     * @return a brief description for inclusion in the application logger.
     * @see Aggregator#getDescriptor()
     */
    String getDescriptor();

}
