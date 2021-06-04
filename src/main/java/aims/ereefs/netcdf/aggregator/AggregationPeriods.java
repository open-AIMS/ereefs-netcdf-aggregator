package aims.ereefs.netcdf.aggregator;

/**
 * Enumerator identifying the periods of aggregation that can be performed.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public enum AggregationPeriods {
    NONE ("None"),
    ALL ("All"),
    DAILY ("Daily"),
    MONTHLY ("Monthly"),
    SEASONAL ("Seasonal"),
    ANNUAL ("Annual");

    public final String description;

    AggregationPeriods(String description) {
        this.description = description;
    }

}
