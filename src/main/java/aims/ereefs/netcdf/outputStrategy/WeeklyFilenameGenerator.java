package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into weeks. This class is
 * incomplete at present and currently only supports the following text templates in the
 * <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;startOfWeek&gt;</b> - the date of the start of the week (monday), formatted as
 *     <code>YYYY-MM-DD</code>.</li>
 * </ul>
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class WeeklyFilenameGenerator extends AbstractOutputFileNameGenerator {

    public WeeklyFilenameGenerator(String pattern, DateUnit dateUnit) {
        super(pattern, dateUnit);
    }

    @Override
    public String generateForTime(double time) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(dateUnit, time);
        LocalDateTime startOfWeek = localDateTime
            .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
            .truncatedTo(ChronoUnit.DAYS);
        final String year = Integer.toString(startOfWeek.getYear());
        final String monthVal = "0" + Integer.toString(startOfWeek.getMonthValue());
        final String month = monthVal.substring(monthVal.length() - 2);
        final String dayVal = "0" + Integer.toString(startOfWeek.getDayOfMonth());
        final String day = dayVal.substring(dayVal.length() - 2);
        final String date = year + "-" + month + "-" + day;
        final String filename = getPattern().replace("<startOfWeek>", date);
        return filename;
    }

    @Override
    public int calculateExpectedTimeIndexes(double time,
                                            AggregationPeriods aggregationPeriods,
                                            int hoursPerTimeIncrement) {

        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(
            dateUnit, time);
        LocalDateTime startOfWeek = localDateTime
            .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
            .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfWeek = localDateTime
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .truncatedTo(ChronoUnit.DAYS);

        switch (aggregationPeriods) {
            case ALL:
                throw new RuntimeException(
                    "Aggregation type \"ALL\" is incompatible with \"Weekly\" files.");
            case DAILY:
                return (int) (startOfWeek.until(endOfWeek, ChronoUnit.DAYS));
            case MONTHLY:
                throw new RuntimeException(
                    "Aggregation type \"MONTHLY\" is incompatible with \"Weekly\" files.");
            case SEASONAL:
                throw new RuntimeException(
                    "Aggregation type \"SEASONAL\" is incompatible with \"Weekly\" files.");
            case ANNUAL:
                throw new RuntimeException(
                    "Aggregation type \"ANNUAL\" is incompatible with \"Weekly\" files.");
            case NONE:
            default:
                return (int) (startOfWeek.until(endOfWeek, ChronoUnit.HOURS) / hoursPerTimeIncrement);
        }

    }

}
