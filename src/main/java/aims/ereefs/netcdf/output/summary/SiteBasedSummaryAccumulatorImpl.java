package aims.ereefs.netcdf.output.summary;

import aims.ereefs.netcdf.input.extraction.ExtractionSite;
import aims.ereefs.netcdf.regrid.IndexWithDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends {@link AbstractSummaryAccumulator} to support accumulating data for specific
 * {@code Sites}, where each {@code Site} is represented as a {@code Bucket}. {@code Sites} are
 * defined via the {@code siteIdToIndexMap} specified in the constructor of this class.
 *
 * @author Aaron Smith
 */
public class SiteBasedSummaryAccumulatorImpl extends AbstractSummaryAccumulator {

    /**
     * Cached map of all extraction sites, including their closest neighbours.
     */
    protected List<ExtractionSite> extractionSiteList;

    /**
     * Constructor to cache the parameters and instantiate the relevant
     * {@link #depthToAccumulationBucketsMap depth} buckets.
     *
     * @param extractionSiteList a list of all sites being extracted.
     */
    public SiteBasedSummaryAccumulatorImpl(List<Double> selectedDepths,
                                           int layerSize,
                                           List<ExtractionSite> extractionSiteList) {
        super(selectedDepths, layerSize);

        // Cache the values.
        this.extractionSiteList = extractionSiteList;

        // Instantiate the buckets for each Site.
        for (Double depth : this.selectedDepths) {
            final Map<String, List<Double>> accumulationBuckets = new HashMap<>();
            this.depthToAccumulationBucketsMap.put(depth, accumulationBuckets);
            for (ExtractionSite extractionSite : this.extractionSiteList) {
                accumulationBuckets.put(extractionSite.getId(), new ArrayList<>());
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

        // Loop through each site.
        for (ExtractionSite extractionSite : this.extractionSiteList) {

            // Loop through each depth.
            for (int depthIndex = 0; depthIndex < this.selectedDepths.size(); depthIndex++) {
                double depth = this.selectedDepths.get(depthIndex);
                Map<String, List<Double>> accumulationBuckets =
                    this.depthToAccumulationBucketsMap.get(depth);

                // Calculate the offset to use based on the depth, since the 3 dimensions (depth,
                // lat, lon) are compressed into a single dimension.
                int depthOffset = depthIndex * layerSize;

                // Accumulate the weighted sum of the closest sites.
                double sum = 0.0;
                double sumWeights = 0.0;
                boolean hasData = false;
                for (IndexWithDistance neighbour : extractionSite.getNeighbours()) {

                    // Ignoring any NaNs, weight the value and add it to the accumulator.
                    double value = timeSliceArray[depthOffset + neighbour.getIndex()];
                    if (!Double.isNaN(value)) {
                        sum += value * neighbour.getWeight();
                        sumWeights += neighbour.getWeight();
                        hasData = true;
                    }
                }

                // Calculate the weighted mean and add it to the bucket..
                if (hasData) {
                    final List<Double> accumulationBucket =
                        accumulationBuckets.get(extractionSite.getId());
                    if (accumulationBucket != null) {
                        accumulationBucket.add(sum / sumWeights);
                    }
                }
            }
        }

    }

}
