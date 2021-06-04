package aims.ereefs.netcdf.input.netcdf;

import ucar.ma2.Array;
import ucar.nc2.units.DateUnit;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * POJO representing a <code>NetCDF</code> input file.
 *
 * @author Aaron Smith
 */
public class InputFileInfo {

    /**
     * An array of aggregated time data that needs to be referenced elsewhere in the system.
     */
    protected Array aggregatedTimeArray = null;
    public Array getAggregatedTimeArray() {
        return this.aggregatedTimeArray;
    }
    public void setAggregatedTimeArray(Array array) {
        this.aggregatedTimeArray = array;
    }

    /**
     * A map that binds an aggregated time (<code>key</code>) to a list of actual times that are
     * grouped into that aggregated time. See
     * {@link aims.ereefs.netcdf.aggregator.time.TimeAggregatorHelper#aggregateTime(double)}} for
     * further explanation and examples.
     */
    protected Map<Double, List<Double>> aggregatedTimeMap = new TreeMap<>();
    public Map<Double, List<Double>> getAggregatedTimeMap() {
        return this.aggregatedTimeMap;
    }

    /**
     * Default buffer size to use when opening a Netcdf Dataset. Default value is -1 to instruct
     * the Netcdf library to use it's own default.
     */
    protected int bufferSize = -1;

    /**
     * Getter method for {@link #bufferSize}.
     *
     * @return the value assigned to {@link #bufferSize}.
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * The <code>DateUnit</code> determined from the <code>TimeVariable</code>.
     */
    protected DateUnit dateUnit;
    public DateUnit getDateUnit() {
        return this.dateUnit;
    }

    /**
     * Cached reference to the calculated end date/time based on the duration of the input file.
     */
    protected LocalDateTime endDateTime;
    public LocalDateTime getEndDateTime() {
        return this.endDateTime;
    }

    /**
     * Cache the number of time indexes expected for this input file/dataset.
     */
    protected int expectedTimeIndexCount = 0;
    public int getExpectedTimeIndexCount() {
        return this.expectedTimeIndexCount;
    }

    /**
     * The input file. This is the primary information, with most other fields populated due to the
     * contents of the file referenced here.
     */
    protected File inputFile = null;
    public File getInputFile() {
        return this.inputFile;
    }

    /**
     * Cached reference to the first date/time in the input file.
     */
    protected LocalDateTime startDateTime;
    public LocalDateTime getStartDateTime() {
        return this.startDateTime;
    }

    /**
     * A list of valid time indexes for the dataset. A time index is considered to be 'valid' if
     * it exists in the time frame covered by the dataset.
     */
    protected Double[] timeIndexes = new Double[] {};
    public Double[] getTimeIndexes() {
        return this.timeIndexes;
    }

    /**
     * Constructor to cache the references.
     *
     * @param inputFile the {@link #inputFile} to cache.
     * @param dateUnit the date unit from the input file.
     */
    public InputFileInfo(File inputFile, DateUnit dateUnit, LocalDateTime startDateTime,
                         LocalDateTime endDateTime, int expectedTimeIndexCount,
                         Double[] timeIndexes, int bufferSize) {
        super();
        this.inputFile = inputFile;
        this.dateUnit = dateUnit;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.expectedTimeIndexCount = expectedTimeIndexCount;
        this.timeIndexes = timeIndexes;
        this.bufferSize = bufferSize;
    }

    public String toString() {
        return this.getClass().getName() + " - " + this.inputFile.getName();
    }

}