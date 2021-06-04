package aims.ereefs.netcdf.input.netcdf;

/**
 * The supported durations of the input NetCDF files to be processed. Refer to
 * {@link InputFileDurationsFactory} for <code>String</code> equivalent.
 *
 * @author Aaron Smith
 */
public enum InputFileDurations {

    /**
     * Represents NetCDF files with a <code>Daily</code> duration. Corresponding {@link #id} is
     * "<code>daily</code>".
     */
    DAILY ("daily"),

    /**
     * Represents NetCDF files with a <code>Monthly</code> duration. Corresponding {@link #id} is
     * "<code>monthly</code>".
     */
    MONTHLY ("monthly");

    /**
     * Property populated with a value that is used by {@link InputFileDurationsFactory} to
     * determine the correct {@link InputFileDurations} instance to return.
     */
    final public String id;

    InputFileDurations(String id) {
        this.id = id;
    }

}
