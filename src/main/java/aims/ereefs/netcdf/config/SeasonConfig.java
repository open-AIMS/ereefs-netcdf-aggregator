package aims.ereefs.netcdf.config;

/**
 * POJO representing a season configuration.
 *
 * <p>
 *     Example JSON:
 * </p>
 *
 * <pre>
 *     {
 *         "{@link #start}": "1-Oct",
 *         "{@link #name}": "wet"
 *     },
 *     {
 *         "{@link #start}": "1-May",
 *         "{@link #name}": "dry"
 *     }
 * </pre>
 *
 * @author Aaron Smith
 */
public class SeasonConfig {

    /**
     * The day of the year on which the season starts.  eg: "1-Oct".
     */
    protected String start;

    /**
     * Getter method for {@link #start}.
     *
     * @return the value assigned to {@link #start}.
     */
    public String getStart() {
        return this.start;
    }

    /**
     * The unique name of the season.
     */
    protected String name = "wet";

    /**
     * Getter method for {@link #name}.
     *
     * @return the value assigned to {@link #name}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Factory method for instantiating and populating an instance of this class.
     *
     * @param name value to assign to {@link #name} property.
     * @param start value to assign to {@link #start} property.
     * @return a new, populated {@link SeasonConfig} instance.
     */
    static public SeasonConfig make(String name, String start) {
        SeasonConfig seasonConfig = new SeasonConfig();
        seasonConfig.name = name;
        seasonConfig.start = start;
        return seasonConfig;
    }

}
