package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.*;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into calendar years. This
 * class supports the following text templates in the <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;year&gt;</b> - the year of the data.</li>
 * </ul>

 * @author Greg Coleman
 * @author Aaron Smith
 */
public class AnnualFilenameGenerator extends AbstractOutputFileNameGenerator {

    /**
     * Constructor to cache the file pattern to use.
     *
     * @param pattern the pattern to use when generating the output filename.
     */
    public AnnualFilenameGenerator(String pattern, DateUnit dateUnit) {
        super(pattern, dateUnit);
    }

    @Override
    public String generateForTime(double time) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(this.dateUnit, time);
        final String year = Integer.toString(localDateTime.getYear());
        final String filename = this.getPattern().replace("<year>", year);
        return filename;
    }

    @Override
    public int calculateExpectedTimeIndexes(double time,
                                            AggregationPeriods aggregationPeriods,
                                            int hoursPerTimeIncrement) {

        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(
            dateUnit, time);
        LocalDateTime startOfYear = localDateTime
            .with(TemporalAdjusters.firstDayOfYear())
            .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfYear = localDateTime
            .with(TemporalAdjusters.lastDayOfYear())
            .plus(1, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS);

        switch (aggregationPeriods) {
            case ALL:
                throw new RuntimeException(
                    "Aggregation type \"ALL\" is incompatible with \"Annual\" files.");
            case DAILY:
                return (int) (startOfYear.until(endOfYear, ChronoUnit.DAYS));
            case MONTHLY:
                return (int) (startOfYear.until(endOfYear, ChronoUnit.MONTHS));
            case SEASONAL:
                throw new RuntimeException(
                    "Aggregation type \"SEASONAL\" is incompatible with \"Annual\" files.");
            case ANNUAL:
                return 1;
            case NONE:
            default:
                return (int) (startOfYear.until(endOfYear, ChronoUnit.HOURS) / hoursPerTimeIncrement);
        }

    }

}
