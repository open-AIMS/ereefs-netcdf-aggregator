package aims.ereefs.netcdf.input.netcdf;

import java.time.temporal.ChronoUnit;

/**
 * {@code Factory} to determine the {@code ChronoUnit} that corresponds to the specified {@code key}.
 *
 * @author Aaron Smith
 */
public class TimeIncrementFactory {

    static public ChronoUnit make(String timeIncrement) {
        if (timeIncrement.equalsIgnoreCase("hourly")) {
            return ChronoUnit.HOURS;
        }
        if (timeIncrement.equalsIgnoreCase("daily")) {
            return ChronoUnit.DAYS;
        }
        if (timeIncrement.equalsIgnoreCase("monthly")) {
            return ChronoUnit.MONTHS;
        }
        if (timeIncrement.equalsIgnoreCase("yearly")) {
            return ChronoUnit.YEARS;
        }
        throw new RuntimeException("Time increment \"" + timeIncrement + "\" not supported.");
    }


}
