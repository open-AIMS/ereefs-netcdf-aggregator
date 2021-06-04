package aims.ereefs.netcdf.aggregator.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.util.*;

/**
 * Abstract implementation of the {@link TimeAggregatorHelper} interface to implement the common
 * {@link #buildAggregatedTimeMap(Double[], int)} method.
 *
 * @author Aaron Smith
 */
public abstract class AbstractTimeAggregatorHelper implements TimeAggregatorHelper {

    protected Logger log = LoggerFactory.getLogger(getClass());

    public Map<Double, List<Double>> buildAggregatedTimeMap(Double[] timeIndexes,
                                                            int maxExpectedTimeIndexes) {
        Map<Double, List<Double>> aggregatedTimeMap = new TreeMap<>();

        long maxTimeIndexes = (
            maxExpectedTimeIndexes < timeIndexes.length ?
                maxExpectedTimeIndexes :
                timeIndexes.length
        );

        // Loop through every value in the time array.
        for (int index = 0; index < maxTimeIndexes; index++) {
            double time = timeIndexes[index];

            // Calculate the corresponding aggregate time.
            double aggregatedTime = this.aggregateTime(time);

            // The map is used to bind a list of corresponding time slices to the aggregated time
            // for simple reference. If this is the first time for this aggregatedTime, bind an
            // empty list to the aggregated time so we can add to it next.
            aggregatedTimeMap.putIfAbsent(aggregatedTime, new ArrayList<>());

            // Add this time slice to the list if it is not already. Checking first ensures each
            // time slice is unique.
            List<Double> groupedTimes = aggregatedTimeMap.get(aggregatedTime);
            if (!groupedTimes.contains(time)) {
                groupedTimes.add(time);
            }

        }

        return aggregatedTimeMap;
    }


}
