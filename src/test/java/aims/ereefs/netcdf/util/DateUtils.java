package aims.ereefs.netcdf.util;

import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import org.assertj.core.api.Assertions;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.units.DateUnit;

import java.time.LocalDateTime;

/**
 * Utilities and constants useful for tests that involve dates and time arrays.
 *
 * @author Aaron Smith
 */
public class DateUtils {

    /**
     * Constant identifying the number of hours per day.
     */
    final static public int HOURS_PER_DAY = 24;

    /**
     * Constant value representing a single hour.
     */
    final static public double HOUR = 1.0 / HOURS_PER_DAY;

    /**
     * The total number of days for which data will be generated.
     */
    final static public int DEFAULT_NUMBER_OF_DAYS = 60;

    /**
     * The number of days in the month of March. This may be useful when checking computations
     * relating to a single month.
     */
    final static public int NUMBER_OF_DAYS_IN_DECEMBER = 31;

    /**
     * Cached reference to the start date/time of the data, retrieved via {@link #getStartTime()}.
     */
    static private Double startDateTime = null;

    /**
     * Cached reference to a <code>DateUnit</code> that is cached/retrieved via
     * {@link #getDateUnit()}.
     */
    static private DateUnit dateUnit = null;

    /**
     * Generate test data for use in test cases. This is an overloaded method that invokes
     * {@link #makeTestData(double,int)} to use the default number of days
     * ({@link #DEFAULT_NUMBER_OF_DAYS}).
     */
    public static Double[] makeTestData() {
        return DateUtils.makeTestData(DateUtils.getStartTime(), DEFAULT_NUMBER_OF_DAYS);
    }

    /**
     * Generate test data for use in test cases. This will be a list of times (as
     * <code>double</code>) that represent hourly increments from {@link #getStartTime()} for the
     * specified number of days.
     */
    public static Double[] makeTestData(double startDateTime, int numberOfDays) {
        Double[] timeIndexes = new Double[numberOfDays * HOURS_PER_DAY];
        int index = 0;
        for (int day = 0; day < numberOfDays; day++) {
            for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
                timeIndexes[index] = startDateTime + day + (HOUR * hour);
                index++;
            }
        }
        return timeIndexes;

    }

    /**
     * Return a cached <code>DateUnit</code> for use in tests.
     */
    static public DateUnit getDateUnit() {
        if (DateUtils.dateUnit == null) {
            try {
                DateUtils.dateUnit = new DateUnit("days since 1990-01-01 00:00:00 +10");
            } catch(Exception fail) {
                Assertions.fail("Failed to instantiate date unit.", fail.getMessage());
            }
        }
        return DateUtils.dateUnit;
    }

    /**
     * The start date/time for the test data. This is the start of the day (midnight) of 1-Dec-2016.
     */
    static public double getStartTime() {
        if (DateUtils.startDateTime == null) {
            DateUtils.startDateTime = NetcdfDateUtils.fromLocalDateTime(
                DateUtils.getDateUnit(), LocalDateTime.of(2016, 12, 1, 0, 0));
        }
        return DateUtils.startDateTime;
    }

}
