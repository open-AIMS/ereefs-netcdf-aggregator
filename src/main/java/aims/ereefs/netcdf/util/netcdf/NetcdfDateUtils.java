package aims.ereefs.netcdf.util.netcdf;

import aims.ereefs.netcdf.util.dataset.DatasetUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by gcoleman on 7/11/2016.
 */
public class NetcdfDateUtils {
    private static final String TIME_UNITS_ATTRIBUTE_NAME = "units";

    public static DateUnit getDateUnit(NetcdfDataset dataset) {

        Variable timeVariable = DatasetUtils.findTimeVariable(dataset);
        if (timeVariable == null) {
            throw new RuntimeException("Time variable not found.");
        }

        return NetcdfDateUtils.getDateUnit(timeVariable);

    }

    public static DateUnit getDateUnit(Variable timeVariable) {

        // Find the attribute that give the epoch.
        final Attribute attr = timeVariable.findAttribute(TIME_UNITS_ATTRIBUTE_NAME);
        if (attr == null) {
            throw new RuntimeException("Could not find time unit attribute " +
                TIME_UNITS_ATTRIBUTE_NAME + " for variable " + timeVariable.getShortName());
        }
        try {
            return new DateUnit(attr.getStringValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate the DateUnit.", e);
        }
    }

    public static LocalDateTime toLocalDateTime(DateUnit dateUnit, Double dateAsDouble) {
        final Date date = dateUnit.makeDate(dateAsDouble);
        final Instant temporalAccessor = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(temporalAccessor, timeZoneOf(dateUnit));

    }

    public static ZonedDateTime toZonedDateTime(DateUnit dateUnit, Double dateAsDouble) {
        final Date date = dateUnit.makeDate(dateAsDouble);
        final Instant temporalAccessor = Instant.ofEpochMilli(date.getTime());
        return ZonedDateTime.ofInstant(temporalAccessor, timeZoneOf(dateUnit));

    }

    public static Double fromLocalDateTime(DateUnit dateUnit, LocalDateTime localDateTime) {
        return fromZonedDateTime(dateUnit, localDateTime.atZone(timeZoneOf(dateUnit)));
    }

    public static Double fromZonedDateTime(DateUnit dateUnit, ZonedDateTime zonedDateTime) {
        final Date date = Date.from(zonedDateTime.toInstant());
        return dateUnit.makeValue(date);
    }


    public static LocalDateTime getLocalDateTime(Array timeArray, DateUnit dateUnit, int index) {
        Double timeDouble = null;
        try {
            timeDouble = timeArray.getDouble(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return NetcdfDateUtils.toLocalDateTime(dateUnit, timeDouble);
    }

    public static ZoneId timeZoneOf(DateUnit dateUnit) {
        String dateString = dateUnit.toString();
        dateString = dateString.substring(dateString.indexOf("since ") + 6);
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("y-M-d H:m:s X");
        final ZonedDateTime d = dateTimeFormatter.parse(dateString, ZonedDateTime::from);
        return d.getZone();

    }


}
