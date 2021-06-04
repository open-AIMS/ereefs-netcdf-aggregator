package aims.ereefs.netcdf.output.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for accumulating data in discrete {@code buckets} based on an index and {@code Depth} of the cell/pixel.
 * A {@code bucket} can represent a contiguous collection of pixels (such as a region), or multiple  individual pixels
 * (such as a site). Data that does not belong to any buckets is ignored.
 *
 * @author Aaron Smith
 */
abstract public class AbstractSummaryAccumulator implements SummaryAccumulator {

    /**
     * Constant to identify a fake depth when no depths are specified. This scenario will arise
     * when the underlying variable does not have depth dimension, such as {@code Wind Speed}.
     */
    protected static double FAKE_DEPTH = 99999.9;

    /**
     * Cached list of depths. Each depth will be divided into zones (even if only a single zone).
     */
    protected List<Double> selectedDepths = new ArrayList<>();

    /**
     * Cached size of a single layer/depth of data.
     */
    protected int layerSize;

    /**
     * A mapping of depth to accumulation buckets for that depth.
     */
    protected Map<Double, Map<String, List<Double>>> depthToAccumulationBucketsMap = new HashMap<>();

    /**
     * Constructor to cache the parameters.
     *
     * @param selectedDepths reference to cache in {@link #selectedDepths}.
     * @param layerSize      value to cache in {@link #layerSize}.
     */
    public AbstractSummaryAccumulator(List<Double> selectedDepths,
                                      int layerSize) {

        // Cache the parameters.
        if (selectedDepths != null && selectedDepths.size() > 0) {
            this.selectedDepths.addAll(selectedDepths);
        } else {
            this.selectedDepths.add(FAKE_DEPTH);
        }
        this.layerSize = layerSize;
    }

    @Override
    public void reset() {
        this.selectedDepths.clear();

        for (Double depthKey : this.depthToAccumulationBucketsMap.keySet()) {
            Map<String, List<Double>> buckets = this.depthToAccumulationBucketsMap.get(depthKey);
            for (String id : buckets.keySet()) {
                buckets.get(id).clear();
            }
        }
    }

    @Override
    public Map<Double, Map<String, List<Double>>> getDepthToAccumulationBucketsMap() {
        return this.depthToAccumulationBucketsMap;
    }

}
