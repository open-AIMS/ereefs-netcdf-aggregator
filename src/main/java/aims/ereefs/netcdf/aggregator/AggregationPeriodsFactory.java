package aims.ereefs.netcdf.aggregator;

/**
 * Factory for obtaining the appropriate {@link AggregationPeriods} instance.
 *
 * @author Aaron Smith
 */
public class AggregationPeriodsFactory {

    /**
     * Factory method to interpret a {@code String} and return the corresponding
     * {@link AggregationPeriods AggregationPeriod}. Values supported are:
     *
     * <ul>
     *     <li>all</li>
     *     <li>daily</li>
     *     <li>monthly</li>
     *     <li>seasonal</li>
     *     <li>none</li>
     * </ul>
     */
    public static AggregationPeriods make(String value) {

        switch (value.toLowerCase()) {
            case "all":
                return AggregationPeriods.ALL;
            case "daily":
                return AggregationPeriods.DAILY;
            case "monthly":
                return AggregationPeriods.MONTHLY;
            case "seasonal":
                return AggregationPeriods.SEASONAL;
            case "annual":
                return AggregationPeriods.ANNUAL;
            default:
                return AggregationPeriods.NONE;
        }

    }

}
