package aims.ereefs.netcdf.output.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specialised utility class that populates a {@link SummaryStatistics} POJO from the data in an
 * unsorted list of {@code Double} values.
 *
 * @author Aaron Smith
 */
public class SummaryStatisticsCalculator {

    /**
     * Accepts a list of {@code Double} data and populates a {@link SummaryStatistics} POJO with
     * the results.
     *
     * @param list the list to analyse. This list is copied before being sorted so the order of the
     *             original list is not changed.
     * @return the corresponding {@link SummaryStatistics}, or {@code null} if the list is empty.
     */
    static public SummaryStatistics calculate(List<Double> list) {

        // Only process if the list contains data.
        if (list.isEmpty()) {
            return null;
        }

        // Copy the list.
        List<Double> internalList = new ArrayList<>(list);

        // Sort the list.
        Collections.sort(internalList);

        // Calculate their values if data exists.
        int size = internalList.size();

        // Mean.
        double sum = internalList.stream().mapToDouble(d -> d).sum();
        double mean = sum / size;

        // Median.
        int index = size / 2;
        if (index >= size) {
            index = size - 1;
        }
        double median = internalList.get(index);

        // 5th percentile.
        index = (int) (Math.round(size * 0.05));
        if (index >= size) {
            index = size - 1;
        }
        double lowPercentile = internalList.get(index);

        // 95th percentile.
        index = (int) (Math.round(size * 0.95));
        if (index >= size) {
            index = size - 1;
        }
        double highPercentile = internalList.get(index);

        // Lowest value.
        double lowest = internalList.get(0);

        // Highest value.
        double highest = internalList.get(size - 1);

        return new SummaryStatistics(
            mean,
            median,
            lowPercentile,
            highPercentile,
            lowest,
            highest
        );

    }

}
