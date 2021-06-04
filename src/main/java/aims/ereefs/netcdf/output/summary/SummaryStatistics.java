package aims.ereefs.netcdf.output.summary;

/**
 * POJO for statistics generated from a list of {@code Double} values.
 *
 * @author Aaron Smith
 */
public class SummaryStatistics {

    /**
     * Cache of the {@code Mean}.
     */
    protected double mean;

    /**
     * Cache of the {@code Median}.
     */
    protected double median;

    /**
     * Cache of the {@code 5th percentile}.
     */
    protected double lowPercentile;

    /**
     * Cache of the {@code 95th percentile}.
     */
    protected double highPercentile;

    /**
     * Cache of the {@code lowest} value.
     */
    protected double lowest;

    /**
     * Cache of the {@code highest} value.
     */
    protected double highest;

    public SummaryStatistics(double mean, double median, double lowPercentile,
                             double highPercentile, double lowest, double highest) {
        this.mean = mean;
        this.median = median;
        this.lowPercentile = lowPercentile;
        this.highPercentile = highPercentile;
        this.lowest = lowest;
        this.highest = highest;
    }

    public double getMean() {
        return this.mean;
    }

    public double getMedian() {
        return this.median;
    }

    public double getLowPercentile() {
        return this.lowPercentile;

    }

    public double getHighPercentile() {
        return highPercentile;
    }

    public double getLowest() {
        return lowest;
    }

    public double getHighest() {
        return highest;
    }

}
