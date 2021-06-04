package aims.ereefs.netcdf.output.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends {@link AbstractSummaryAccumulator} to support accumulating data within {@code Zones},
 * where each {@code Zone} is represented as a {@code Bucket}. {@code Zones} are defined via the
 * {@code indexToZoneIdMap} specified in the constructor of this class.
 *
 * @author Aaron Smith
 */
public class ZoneBasedSummaryAccumulatorImpl extends AbstractSummaryAccumulator {

    /**
     * Cached map of an index of a cell/pixel to a zone/region.
     */
    protected List<String> indexToZoneIdMap;

    /**
     * Constructor to cache the parameters and instantiate the relevant
     * {@link #depthToAccumulationBucketsMap depth} buckets.
     *
     * @param indexToZoneIdMap ths map of an index of a cell/pixel to a zone/region.
     */
    public ZoneBasedSummaryAccumulatorImpl(List<Double> selectedDepths,
                                           int layerSize,
                                           List<String> indexToZoneIdMap) {
        super(selectedDepths, layerSize);

        // Cache the parameters.
        this.indexToZoneIdMap = indexToZoneIdMap;

        // Build a list of unique zones, ignoring any "nulls".
        List<String> uniqueZoneIds = new ArrayList<>();
        for (String zoneId : indexToZoneIdMap) {
            if ((zoneId != null) && (!uniqueZoneIds.contains(zoneId))) {
                uniqueZoneIds.add(zoneId);
            }
        }

        // Instantiate the buckets.
        for (Double depth : this.selectedDepths) {
            final Map<String, List<Double>> accumulationBuckets = new HashMap<>();
            this.depthToAccumulationBucketsMap.put(depth, accumulationBuckets);
            for (String zoneId : uniqueZoneIds) {
                accumulationBuckets.put(zoneId, new ArrayList<>());
            }
        }
    }

    /**
     * Allocate each data/pixel to the depth/zone bucket it belongs to. If more than one
     * {@code timeSliceArray} is specified, only the first array is used.
     *
     * @param timeSliceArrays one or more data arrays (Double[]). This method only uses the first
     *                        data array.
     */
    @Override
    public void add(List<Double[]> timeSliceArrays) {
        Double[] timeSliceArray = timeSliceArrays.get(0);

        // Loop through each data point in the timeSlice.
        for (int index = 0; index < timeSliceArray.length; index++) {

            // Only consider the value if it is not a NaN.
            double value = timeSliceArray[index];

            if (!Double.isNaN(value)) {

                // A timeslice is arranged so that each pixel/cell for a depth is stored
                // sequentially, and each depth is then appended. For example, with a layer that
                // is 4 x 4, with 3 depths, the first 16 data points are for the 1st depth, the
                // next 16 data points are for the next depth, etc. To calculate which depth layer
                // is being processed (zero based), divide the data point index by the layer size.
                // To calculate which cell in the layer is being processed, it's the remainder of
                // that calculation. For example, consider data point 22 in the above example.
                // 22 / 16 = 1.375, or "1 with 8 remaining". This means it is the 2nd depth (depth
                // is zero based, so "1" means "2nd"), and the 8th cell in the layer.
                int depthIndex = index / layerSize;
                Double depth = this.selectedDepths.get(depthIndex);
                Map<String, List<Double>> accumulationBuckets = this.depthToAccumulationBucketsMap.get(depth);

                // Determine the index of the cell within the layer.
                int cellIndex = index % layerSize;

                // Identify the zone and bucket for the index.
                String zoneId = this.indexToZoneIdMap.get(cellIndex);
                List<Double> accumulationBucket = accumulationBuckets.get(zoneId);

                // Add the data to the bucket.
                if (accumulationBucket != null) {
                    accumulationBucket.add(value);
                }

            }

        }
    }

}
