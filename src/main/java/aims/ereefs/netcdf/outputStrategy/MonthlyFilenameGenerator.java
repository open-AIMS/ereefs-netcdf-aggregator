package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into monthly files. This
 * class supports the following text templates in the <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;year&gt;</b> - the year of the data.</li>
 *     <li><b>&lt;month&gt;</b> - the month of the data.</li>
 * </ul>

 * @author Greg Coleman
 * @author Aaron Smith
 */
public class MonthlyFilenameGenerator extends AbstractOutputFileNameGenerator {

    /**
     * Constructor to cache the file pattern to use.
     *
     * @param pattern the pattern to use when generating the output filename.
     */
    public MonthlyFilenameGenerator(String pattern, DateUnit dateUnit) {
        super(pattern, dateUnit);
    }

    @Override
    public String generateForTime(double time) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(this.dateUnit, time);
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

        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(
            dateUnit, time);
        LocalDateTime startOfMonth = localDateTime
            .with(TemporalAdjusters.firstDayOfMonth())
            .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfMonth = localDateTime
            .with(TemporalAdjusters.lastDayOfMonth())
            .plus(1, ChronoUnit.DAYS)
            .truncatedTo(ChronoUnit.DAYS);

        switch (aggregationPeriods) {
            case ALL:
                throw new RuntimeException(
                    "Aggregation type \"ALL\" is incompatible with \"Monthly\" files.");
            case DAILY:
                return (int) (startOfMonth.until(endOfMonth, ChronoUnit.DAYS));
            case MONTHLY:
                return 1;
            case SEASONAL:
                throw new RuntimeException(
                    "Aggregation type \"SEASONAL\" is incompatible with \"Monthly\" files.");
            case ANNUAL:
                throw new RuntimeException(
                    "Aggregation type \"ANNUAL\" is incompatible with \"Monthly\" files.");
            case NONE:
            default:
                return (int) (startOfMonth.until(endOfMonth, ChronoUnit.HOURS) / hoursPerTimeIncrement);
        }

    }

}
