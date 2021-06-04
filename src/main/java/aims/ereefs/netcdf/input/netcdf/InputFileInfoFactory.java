package aims.ereefs.netcdf.input.netcdf;

import aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper;
import aims.ereefs.netcdf.util.dataset.DatasetUtils;
import aims.ereefs.netcdf.util.netcdf.ArrayUtils;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateUnit;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory class for instantiating and initialising a {@link InputFileInfo} object based on a
 * specified dataset. This factory assumes that all datasets processed by this factory use the same
 * <code>DateUnit</code>.
 *
 * @author Aaron Smith
 */
public class InputFileInfoFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cache a reference to a {@code DateUnit} for use interpreting times/dates. This reference will
     * be used for all datasets processed by this factory.
     */
    protected DateUnit cachedDateUnit = null;

    /**
     * Cached reference to the {@code bufferSize}.
     */
    protected int bufferSize;

    /**
     * Cached reference to the {@code hoursPerTimeIndex}. For example, {@code 1} if each time index
     * represents an {@code hour}, or {@code 24} if each time index represents a {@code day}.
     */
    protected int hoursPerTimeIndex;

    /**
     * Cached reference to the {@link InputFileDurations} to identify the length of time the input
     * file represents. For example, {@link InputFileDurations#DAILY Daily} or
     * {@link InputFileDurations#MONTHLY Monthly}.
     */
    protected InputFileDurations inputFileDuration;

    /**
     * Cached reference to the {@link TimeAggregatorHelper} instance to use for aggregating time
     * indexes.
     */
    protected TimeAggregatorHelper timeAggregatorHelper;

    /**
     * Constructor to cache the references.
     */
    public InputFileInfoFactory(InputFileDurations inputFileDuration, int hoursPerTimeIndex,
                                int bufferSize) {
        super();
        this.inputFileDuration = inputFileDuration;
        this.hoursPerTimeIndex = hoursPerTimeIndex;
        this.bufferSize = bufferSize;
    }

    /**
     * Setter method to allow the {@link #timeAggregatorHelper} reference to be set after
     * instantiation.
     */
    public void setTimeAggregatorHelper(TimeAggregatorHelper helper) {
        this.timeAggregatorHelper = helper;
    }

    /**
     * Instantiate and populate an {@link InputFileInfo} object to represent the specified
     * {@code File}. This factory method will also parse the time indexes to remove any that fall
     * outside of the valid time indexes for the input file.
     *
     * @param file the file to use to populate the return object.
     * @return an {@link InputFileInfo} object based on the specified {@code file}.
     */
    public InputFileInfo make(File file) {

        // Retrieve the raw timeIndexes from the dataset. If we don't already have a reference to a
        // DateUnit, cache the reference to the one used in the input file/dataset.
        Double[] rawTimeIndexes = null;
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(file.getAbsolutePath());

            // Ensure the dataset has a time variable.
            Variable timeVariable = DatasetUtils.findTimeVariable(dataset);
            if (timeVariable == null) {
                throw new RuntimeException("Time variable not found.");
            }

            // Cache the date unit if not already.
            if (this.cachedDateUnit == null) {
                this.cachedDateUnit = NetcdfDateUtils.getDateUnit(timeVariable);
            }
            DateUnit dateUnit = this.cachedDateUnit;
            if (dateUnit == null) {
                throw new RuntimeException("Unable to identify DateUnit from time variable.");
            }

            // Read the time indexes from the dataset while we have a reference to the TimeVariable.
            Array rawTimeArray = timeVariable.read();
            if (rawTimeArray.getSize() == 0) {
                throw new RuntimeException("Time variable contains no data.");
            }
            rawTimeIndexes = ArrayUtils.asJavaDoubleArray(rawTimeArray);

        } catch (Exception e) {
            throw new RuntimeException("Error thrown while reading time series from file \"" +
                file.getAbsolutePath() + "\".", e);
        }

        // Use the first time index in the dataset to calculate the valid start date/time for the
        // dataset. Note that this was an assumption listed earlier.
        final double firstTimeIndex = rawTimeIndexes[0];
        final LocalDateTime startDateTime = NetcdfDateUtils.toLocalDateTime(
            this.cachedDateUnit, firstTimeIndex);

        // Calculate the end date/time based on the file duration.
        LocalDateTime endDateTime = null;

        // DAILY input file.
        if (this.inputFileDuration == InputFileDurations.DAILY) {

            // End date/time is the beginning of the following day.
            endDateTime = startDateTime
                .plus(1, ChronoUnit.DAYS);

        }

        // MONTHLY input file.
        if (this.inputFileDuration == InputFileDurations.MONTHLY) {

            // End date/time is the beginning of the following month.
            endDateTime = startDateTime
                .with(TemporalAdjusters.lastDayOfMonth())
                .plus(1, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS);

        }

        // If the endDateTime has not been defined, use the last time index in the dataset.
        if (endDateTime == null) {
            final double lastTimeIndex = rawTimeIndexes[rawTimeIndexes.length - 1];
            endDateTime = NetcdfDateUtils.toLocalDateTime(this.cachedDateUnit, lastTimeIndex);
        }

        // Calculate the number of time indexes expected based on start/end date and hours per
        // time index.
        int expectedTimeIndexCount =
            (int) (startDateTime.until(endDateTime, ChronoUnit.HOURS) / this.hoursPerTimeIndex);

        // Build the list of valid time indexes.
        List<Double> validTimeIndexes = new ArrayList<>();
        for (int index = 0; index < rawTimeIndexes.length; index++) {
            Double timeIndex = rawTimeIndexes[index];
            LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(
                this.cachedDateUnit, timeIndex);
            if ((startDateTime.isBefore(localDateTime) || startDateTime.isEqual(localDateTime)) &&
                localDateTime.isBefore(endDateTime)) {
                validTimeIndexes.add(timeIndex);
            }
        }

        // Convert the valid time indexes list to an array and cache. Log any time indexes that
        // have been removed.
        List<Double> originalList = new ArrayList<>(Arrays.asList(rawTimeIndexes));
        Double[] timeIndexes = new Double[validTimeIndexes.size()];
        for (int index = 0; index < validTimeIndexes.size(); index++) {
            Double validTimeIndex = validTimeIndexes.get(index);
            timeIndexes[index] = validTimeIndex;
            originalList.remove(originalList.indexOf(validTimeIndex));
        }

        // Write some logging.
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("raw: " + rawTimeIndexes.length);
            sb.append(" | ");
            sb.append("valid: " + timeIndexes.length);
            sb.append(" | ");
            sb.append("expected: " + expectedTimeIndexCount);
            sb.append(" | ");
            if (originalList.size() > 0) {
                sb.append("rejected: " + originalList.stream().map(dbl -> dbl.toString()).collect(Collectors.joining(",")));
            }
            logger.debug(sb.toString());
        }

        return new InputFileInfo(file, this.cachedDateUnit, startDateTime, endDateTime,
            expectedTimeIndexCount, timeIndexes, bufferSize);

    }

    /**
     * Specialist method for populating aggregation times based on the
     * {@link InputFileInfo#timeIndexes}.
     */
    public InputFileInfo populateAggregatedTimes(InputFileInfo inputFileInfo) {

        // Use the TimeAggregatorHelper to build a map of AggregatedTime values linked to all
        // TimeIndexes within the file that fall within that AggregatedTime, and cache the map to
        // the InputFileInfo object.
        inputFileInfo.aggregatedTimeMap.clear();
        inputFileInfo.aggregatedTimeMap.putAll(
            this.timeAggregatorHelper.buildAggregatedTimeMap(
                inputFileInfo.timeIndexes,
                inputFileInfo.expectedTimeIndexCount
            )
        );

        // Use the keys from AggregatedTimeMap to build an AggregatedTimeArray which is cached for
        // use by other parts of the application.
        Set<Double> aggregatedTimes = inputFileInfo.aggregatedTimeMap.keySet();
        inputFileInfo.aggregatedTimeArray = new ArrayDouble.D1(aggregatedTimes.size());
        int index = 0;
        for (Double time : aggregatedTimes) {
            inputFileInfo.aggregatedTimeArray.setDouble(index, time);
            index++;
        }
        logger.debug("# of aggregated times: " + inputFileInfo.aggregatedTimeArray.getSize());

        return inputFileInfo;

    }

}
