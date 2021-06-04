package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into daily files. This
 * class supports the following text templates in the <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;date&gt;</b> - the date of the data.</li>
 * </ul>

 * @author Greg Coleman
 * @author Aaron Smith
 */
public class DailyFilenameGenerator extends AbstractOutputFileNameGenerator {

    public DailyFilenameGenerator(String pattern, DateUnit dateUnit) {
        super(pattern, dateUnit);
    }

    @Override
    public String generateForTime(double time) {
        final LocalDateTime localDateTime = NetcdfDateUtils.toLocalDateTime(this.dateUnit, time);
        final String year = Integer.toString(localDateTime.getYear());
        final String monthVal = "0" + Integer.toString(localDateTime.getMonthValue());
        final String month = monthVal.substring(monthVal.length() - 2);
        final String dayVal = "0" + Integer.toString(localDateTime.getDayOfMonth());
        final String day = dayVal.substring(dayVal.length() - 2);
        final String date = year + "-" + month + "-" + day;
        final String filename = getPattern().replace("<date>", date);
        return filename;
    }

    @Override
    public int calculateExpectedTimeIndexes(double time,
                                            AggregationPeriods aggregationPeriods,
                                            int hoursPerTimeIncrement) {

        switch (aggregationPeriods) {
            case ALL:
                throw new RuntimeException(
                    "Aggregation type \"ALL\" is incompatible with \"Daily\" files.");
            case DAILY:
                return 1;
            case MONTHLY:
                throw new RuntimeException(
                    "Aggregation type \"MONTHLY\" is incompatible with \"Daily\" files.");
            case SEASONAL:
                throw new RuntimeException(
                    "Aggregation type \"SEASONAL\" is incompatible with \"Daily\" files.");
            case ANNUAL:
                throw new RuntimeException(
                    "Aggregation type \"ANNUAL\" is incompatible with \"Daily\" files.");
            case NONE:
            default:
                return 24 / hoursPerTimeIncrement;
        }

    }

}
