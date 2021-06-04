package aims.ereefs.netcdf.aggregator.time;

import aims.ereefs.netcdf.config.SeasonConfig;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Concrete implementation of the {@link TimeAggregatorHelper} interface to support
 * <code>Seasonal</code> aggregations.
 *
 * @author Aaron Smith
 */
public class SeasonalTimeAggregatorHelper extends AbstractTimeAggregatorHelper implements
    TimeAggregatorHelper {

    private final DateUnit dateUnit;

    /**
     * Helper class for parsing dates.
     */
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m");

    private List<SeasonConfig> seasonConfigs;

    /**
     * Internal cache of season start dates, derived from {@link #seasonConfigs}, used to determine
     * the most recent start of a season for a date/time value. This cache is built by
     * {@link #addSeasonStartsForYear(int)}.
     */
    private List<LocalDateTime> seasonStarts = new ArrayList<>();

    /**
     * Internal map that can be used to identify the corresponding {@link SeasonConfig} for a
     * specified {@link #seasonStarts} value. This map is built by
     * {@link #addSeasonStartsForYear(int)}.
     */
    private Map<LocalDateTime, SeasonConfig> seasonStartToSeasonConfigMap = new HashMap<>();

    /**
     * Internal cache of the years for which {@link #seasonStarts} have been calculated.
     */
    private List<Integer> seasonStartYearsCovered = new ArrayList<>();

    public SeasonalTimeAggregatorHelper(DateUnit dateUnit, List<SeasonConfig> seasonConfigs) {
        super();
        this.dateUnit = dateUnit;
        this.seasonConfigs = seasonConfigs;
    }

    /**
     * {@code Season}-specific implementation of the method, converting the date to the first
     * day of the corresponding season.
     *
     * @see #calculateSeasonStart(double)
     */
    public double aggregateTime(double d) {
        return NetcdfDateUtils.fromLocalDateTime(this.dateUnit, this.calculateSeasonStart(d));
    }

    /**
     * Utility method to identify the first day of the season that contains the specified date.
     *
     * @param date the date of interest.
     * @return the first day of the season that contains the date of interest.
     */
    protected LocalDateTime calculateSeasonStart(double date) {

        // Convert the date to an object we can work with.
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(dateUnit, date);
        final int currentYear = localDateTime.getYear();

        // Check if any season starts have been populated.  If not, populate for the previous year
        // and add them to the internal caches. Note that we will populate for the current year
        // next.
        if (this.seasonStartYearsCovered.isEmpty()) {
            final int previousYear = currentYear - 1;
            this.seasonStartYearsCovered.add(previousYear);
            this.addSeasonStartsForYear(previousYear);
        }

        // Check if the season starts for the current year have been populated. If not, populate
        // them and add them to the internal caches.
        if (!this.seasonStartYearsCovered.contains(currentYear)) {
            this.seasonStartYearsCovered.add(currentYear);
            this.addSeasonStartsForYear(currentYear);
        }

        // Check if the season starts for the next year have been populated. If not, populate
        // them and add them to the internal caches. This helps to prevent IndexOutOfBoundExceptions.
        final int nextYear = currentYear + 1;
        if (!this.seasonStartYearsCovered.contains(nextYear)) {
            this.seasonStartYearsCovered.add(nextYear);
            this.addSeasonStartsForYear(nextYear);
        }

        // Find the latest season start which is less than the current date/time.
        LocalDateTime latestSeasonStart = null;
        for (LocalDateTime seasonStart : this.seasonStarts) {
            if (latestSeasonStart == null) {
                latestSeasonStart = seasonStart;
            }
            boolean isLaterOrOn = localDateTime.isAfter(seasonStart) || localDateTime.isEqual(seasonStart);
            if (isLaterOrOn && seasonStart.isAfter(latestSeasonStart)) {
                latestSeasonStart = seasonStart;
            }
        }
        return latestSeasonStart;
    }

    /**
     * Returns the {@link SeasonConfig} for the season that contains the specified date. This method
     * invokes {@link #calculateSeasonStart(double)} to identify the first day of the corresponding
     * season, and then uses {@link #seasonStartToSeasonConfigMap} to look up the corresponding
     * {@link SeasonConfig}.
     *
     * @param date the date of interest. This can be any date within the season and will return the
     *             same value.
     * @return the {@link SeasonConfig} that contains the date of interest.
     */
    public SeasonConfig findSeasonConfigByDate(double date) {
        LocalDateTime seasonStart = this.calculateSeasonStart(date);
        return this.seasonStartToSeasonConfigMap.get(seasonStart);
    }

    /**
     * Returns the {@code index} of the {@link SeasonConfig} that matches the season that contains
     * the specified date.
     *
     * @param date the date of interest.
     * @return the index of the {@link SeasonConfig} within the {@link #seasonConfigs list}.
     * @see #findSeasonConfigByDate(double)
     */
    public int findSeasonConfigIndexByDate(double date) {
        SeasonConfig seasonConfig = this.findSeasonConfigByDate(date);
        return this.seasonConfigs.indexOf(seasonConfig);
    }

    /**
     * Utility method to increase the {@link #seasonStarts} list with the list of seasons (see
     * {@link #seasonConfigs} for the specified year. {@link #seasonStarts} is then sorted
     * chronologically.
     *
     * @param year the <code>year</code> of interest.
     */
    protected void addSeasonStartsForYear(int year) {
        for (SeasonConfig seasonConfig : this.seasonConfigs) {
            LocalDateTime seasonStart = LocalDateTime.parse(
                year + "-" + seasonConfig.getStart() + " 0:0",
                dateFormatter
            );
            this.seasonStarts.add(seasonStart);
            this.seasonStartToSeasonConfigMap.put(seasonStart, seasonConfig);
        }
        Collections.sort(this.seasonStarts);
    }

    @Override
    public String getDescriptor() {
        return "Seasonal";
    }

}
