package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;

/**
 * Created by gcoleman on 7/11/2016.
 * Use the descendents of this class to determine when to create a new output file and what to name it
 */
public interface OutputFileNameGenerator {

    /**
     * Common method to return the template pattern used by the implementing class for generating
     * filenames.
     */
    String getPattern();

    /**
     * Generate a filename suitable for the specified time based on the {@link OutputFileStrategy}
     * of the implementing class.
     */
    String generateForTime(double time);

    /**
     * Calculate the expected number of time indexes for the file based on the specified time and
     * aggregation type and hours per time increment.
     */
    int calculateExpectedTimeIndexes(double time,
                                     AggregationPeriods aggregationPeriods,
                                     int hoursPerTimeIncrement);

}
