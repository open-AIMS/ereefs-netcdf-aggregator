package aims.ereefs.netcdf.output.summary;

import java.util.List;
import java.util.Map;

/**
 * Public interface for classes that support the accumulation of data for summary purposes.
 * Implementations of this interface will support summarising based on {@code Zones} (eg: regions)
 * or {@code Sites}.
 *
 * @author Aaron Smith
 */
public interface SummaryAccumulator {

    /**
     * A single input time slice from the dataset. If more than one {@code timeSliceArray} is
     * specified, only the first array is used.
     *
     * @param timeSliceArrays one or more data arrays (Double[]). This method only uses the first
     *                        data array.
     */
    void add(List<Double[]> timeSliceArrays);

    /**
     * Reset the contents of each of the buckets, ready for the next aggregation period.
     */
    void reset();

    /**
     * Return the accumulated data, most likely for analysis.
     *
     * @return the data accumulated by specialisation.
     */
    Map<Double, Map<String, List<Double>>> getDepthToAccumulationBucketsMap();

}
