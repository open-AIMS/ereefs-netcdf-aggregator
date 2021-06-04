package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.aggregator.time.SeasonalTimeAggregatorHelper;
import aims.ereefs.netcdf.config.SeasonConfig;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.util.List;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into seasons. This class is
 * incomplete at present and currently only supports the following text templates in the
 * <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;year&gt;</b> - the year of the start of the season.</li>
 *     <li><b>&lt;month&gt;</b> - the month of the start of the season.</li>
 * </ul>
 *
 * The intention is to expand this class to support the following text templates in the
 * <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;index&gt;</b> - zero-based index of season's position within the defined
 *     seasons.</li>
 *     <li><b>&lt;name&gt;</b> - the defined name of the season.</li>
 * </ul>
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class SeasonalFilenameGenerator extends AbstractOutputFileNameGenerator {

    protected SeasonalTimeAggregatorHelper timeAggregatorHelper;

    protected List<SeasonConfig> seasons;

    /**
     * Constructor to cache the file pattern to use.
     *
     * @param pattern the pattern to use when generating the output filename.
     */
    public SeasonalFilenameGenerator(String pattern, DateUnit dateUnit, List<SeasonConfig> seasons) {
        super(pattern, dateUnit);
        this.seasons = seasons;
        this.timeAggregatorHelper = new SeasonalTimeAggregatorHelper(dateUnit, seasons);
    }

    @Override
    public String generateForTime(double time) {
        double seasonStart = timeAggregatorHelper.aggregateTime(time);
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(this.dateUnit, seasonStart);
        String year = Integer.toString(localDateTime.getYear());
        String val = "0" + Integer.toString(localDateTime.getMonthValue());
        String month = val.substring(val.length() - 2);
        String filename = this.getPattern()
            .replace("<year>", year)
            .replace("<month>", month);
        return filename;
    }

    @Override
    public int calculateExpectedTimeIndexes(double time,
                                            AggregationPeriods aggregationPeriods,
                                            int hoursPerTimeIncrement) {

        // Seasonal filename generation has been deprecated.
        // See notes in OutputFilenameGeneratorFactory.
        throw new UnsupportedOperationException("Support for Seasonal output files deprecated.");

    }

}
