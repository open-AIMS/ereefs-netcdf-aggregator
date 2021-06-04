package aims.ereefs.netcdf.util.netcdf;

import au.gov.aims.netcdf.Generator;
import au.gov.aims.netcdf.bean.*;
import org.joda.time.*;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * Utility for generating a NetCDF file.
 *
 * @author Aaron Smith
 */
public class NetcdfFileGenerator {

    /**
     * Name of variable whose values contain random values.
     */
    final static public String TEMPERATURE_RANDOM_VARIABLE_NAME = "temp_random";

    /**
     * Name of variable whose values contain the number of time increments since the start of the
     * file. That is, all values for that time increment (irrespective of depth, latitude or
     * longitude) will match.
     */
    final static public String TEMPERATURE_TIME_INCREMENTS_SINCE_START_VARIABLE_NAME = "temp_since_start";

    /**
     * Name of variable whose values contain the number of time increments since {@code epoch}.
     * That is, all values for that time increment (irrespective of depth, latitude or longitude)
     * will match.
     */
    final static public String TEMPERATURE_TIME_INCREMENTS_SINCE_EPOCH_VARIABLE_NAME = "temp_since_epoch";

    /**
     * Name of variable whose values contain the depth value of the cell. That is, all values for
     * that depth (irrespective of time increment, latitude or longitude) will match.
     */
    final static public String TEMPERATURE_DEPTH_VARIABLE_NAME = "temp_depth";

    /**
     * Name of variable whose values contain the latitude value of the cell. That is, all values
     * for that latitude (irrespective of time increment, depth or longitude) will match.
     */
    final static public String TEMPERATURE_LAT_VARIABLE_NAME = "temp_lat";

    /**
     * Name of variable whose values contain the longitude value of the cell. That is, all values
     * for that latitude (irrespective of time increment, depth or latitude) will match.
     */
    final static public String TEMPERATURE_LON_VARIABLE_NAME = "temp_lon";

    final static public String WIND_SPEED_EASTWARD_VARIABLE_NAME = "wspeed_u";
    final static public String WIND_SPEED_NORTHWARD_VARIABLE_NAME = "wspeed_v";
    final static public String CURRENT_EASTWARD_VARIABLE_NAME = "u";
    final static public String CURRENT_NORTHWARD_VARIABLE_NAME = "v";

    /**
     * An array of variable names used in the Netcdf file.
     */
    final static public String[] VARIABLE_NAMES = new String[]{
        TEMPERATURE_RANDOM_VARIABLE_NAME,
        TEMPERATURE_TIME_INCREMENTS_SINCE_START_VARIABLE_NAME,
        TEMPERATURE_TIME_INCREMENTS_SINCE_EPOCH_VARIABLE_NAME,
        TEMPERATURE_DEPTH_VARIABLE_NAME,
        TEMPERATURE_LAT_VARIABLE_NAME,
        TEMPERATURE_LON_VARIABLE_NAME,
        WIND_SPEED_EASTWARD_VARIABLE_NAME,
        WIND_SPEED_NORTHWARD_VARIABLE_NAME,
        CURRENT_EASTWARD_VARIABLE_NAME,
        CURRENT_NORTHWARD_VARIABLE_NAME
    };

    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");
    public static final DateTime DEFAULT_START_DATE = new DateTime(2015, 1, 1, 0, 0, TIMEZONE_BRISBANE);

    static public File generateHourlyDaily(
        File path,
        boolean missingData,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        return generateHourlyDaily(path, missingData, DEFAULT_START_DATE, lats, lons, depths);
    }

    static public File generateHourlyDaily(
        File path,
        boolean missingData,
        DateTime startDate,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        DateTime endDate = startDate.plusHours(24);
        return generate(path, startDate, endDate, ChronoUnit.HOURS, missingData, lats, lons, depths);
    }

    static public File generateDailyMonthly(
        File path,
        boolean missingData,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        return generateDailyMonthly(path, missingData, DEFAULT_START_DATE, lats, lons, depths);
    }

    static public File generateDailyMonthly(
        File path,
        boolean missingData,
        DateTime startDate,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        DateTime endDate = startDate.plusMonths(1);
        return generate(path, startDate, endDate, ChronoUnit.DAYS, missingData, lats, lons, depths);
    }

    static public File generateMonthlyMonthly(
        File path,
        boolean missingData,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        return generateMonthlyMonthly(path, missingData, DEFAULT_START_DATE, lats, lons, depths);
    }

    static public File generateMonthlyMonthly(
        File path,
        boolean missingData,
        DateTime startDate,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {
        DateTime endDate = startDate.plusMonths(1);
        return generate(path, startDate, endDate, ChronoUnit.MONTHS, missingData, lats, lons, depths);
    }

    static public File generate(
        File path,
        DateTime startDate,
        DateTime endDate,
        ChronoUnit timeIncrement,
        boolean missingData,
        float[] lats,
        float[] lons,
        double[] depths
    ) throws IOException, InvalidRangeException {

        // Create the file.
        final File file = new File(
            path.getAbsolutePath() + File.separator + timeIncrement.name() + "-" + UUID.randomUUID().toString() + ".nc"
        );

        Generator netCDFGenerator = new Generator();

        NetCDFDataset dataset = new NetCDFDataset();
        dataset.setGlobalAttribute("title", timeIncrement.name() + " test file");
        dataset.setGlobalAttribute("timeSteps", timeIncrement.name());

        // Value = random
        NetCDFTimeDepthVariable tempRandomVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_RANDOM_VARIABLE_NAME,
            "degrees C"
        );
        tempRandomVar.setAttribute("long_name", "Temperature (random)");
        dataset.addVariable(tempRandomVar);

        // Value = time increments since start
        NetCDFTimeDepthVariable tempTimeIncrementsSinceStartVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_TIME_INCREMENTS_SINCE_START_VARIABLE_NAME,
            "degrees C"
        );
        tempTimeIncrementsSinceStartVar.setAttribute("long_name", "Temperature (time increments since start)");
        dataset.addVariable(tempTimeIncrementsSinceStartVar);

        // Value = time increments since start
        NetCDFTimeDepthVariable tempTimeIncrementsSinceEpochVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_TIME_INCREMENTS_SINCE_EPOCH_VARIABLE_NAME,
            "degrees C"
        );
        tempTimeIncrementsSinceEpochVar.setAttribute("long_name", "Temperature (time increments since epoch)");
        dataset.addVariable(tempTimeIncrementsSinceEpochVar);

        // Value = depth
        NetCDFTimeDepthVariable tempDepthVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_DEPTH_VARIABLE_NAME,
            "degrees C"
        );
        tempDepthVar.setAttribute("long_name", "Temperature (depth)");
        dataset.addVariable(tempDepthVar);

        // Value = lat
        NetCDFTimeDepthVariable tempLatVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_LAT_VARIABLE_NAME,
            "degrees C"
        );
        tempLatVar.setAttribute("long_name", "Temperature (latitude)");
        dataset.addVariable(tempLatVar);

        // Value = lon
        NetCDFTimeDepthVariable tempLonVar = new NetCDFTimeDepthVariable(
            TEMPERATURE_LON_VARIABLE_NAME,
            "degrees C"
        );
        tempLonVar.setAttribute("long_name", "Temperature (longitude)");
        dataset.addVariable(tempLonVar);

        // Wind speed.
        NetCDFTimeVariable wspeed_uVar = new NetCDFTimeVariable(
            WIND_SPEED_EASTWARD_VARIABLE_NAME,
            "ms-1"
        );
        wspeed_uVar.setAttribute("long_name", "eastward_wind");
        NetCDFTimeVariable wspeed_vVar = new NetCDFTimeVariable(
            WIND_SPEED_NORTHWARD_VARIABLE_NAME,
            "ms-1"
        );
        wspeed_vVar.setAttribute("long_name", "northward_wind");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeVariable>("wind", wspeed_uVar, wspeed_vVar));

        // Current speed.
        NetCDFTimeDepthVariable uVar = new NetCDFTimeDepthVariable(
            CURRENT_EASTWARD_VARIABLE_NAME,
            "ms-1"
        );
        uVar.setAttribute("long_name", "Eastward current");
        NetCDFTimeDepthVariable vVar = new NetCDFTimeDepthVariable(
            CURRENT_NORTHWARD_VARIABLE_NAME,
            "ms-1"
        );
        vVar.setAttribute("long_name", "Northward current");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeDepthVariable>("sea_water_velocity", uVar, vVar));

        // Depth of sea-bed.
        NetCDFVariable botzVar = new NetCDFVariable("botz", "metre");
        botzVar.setAttribute("long_name", "Depth of sea-bed");
        dataset.addVariable(botzVar);

        // Build the list of frame dates based on start date, end date, and increment.
        List<DateTime> frameDates = FrameDatesGeneratorFactory
            .make(timeIncrement)
            .generate(startDate, endDate);

        // Calculate the start and end indexes based on the time increments.
        int startTimeIndex = Hours.hoursBetween(dataset.getTimeEpoch(), startDate).getHours();
        int endTimeIndex = Hours.hoursBetween(dataset.getTimeEpoch(), endDate).getHours();

        // Populate the dataset.
        final Random random = new Random();
        for (float lat : lats) {
            for (float lon : lons) {

                // Set data for NetCDFVariable
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);

                for (DateTime frameDate : frameDates) {
                    final int hourIndex = Hours.hoursBetween(dataset.getTimeEpoch(), frameDate).getHours();

                    // Calculate the number of time increments since start and epoch for use in
                    // populating variables.
                    int incrementsSinceStart = 0;
                    int incrementsSinceEpoch = 0;
                    if (timeIncrement.equals(ChronoUnit.HOURS)) {
                        incrementsSinceEpoch = Hours.hoursBetween(dataset.getTimeEpoch(), frameDate).getHours();
                        incrementsSinceStart = Hours.hoursBetween(startDate, frameDate).getHours();
                    }
                    if (timeIncrement.equals(ChronoUnit.DAYS)) {
                        incrementsSinceEpoch = Days.daysBetween(dataset.getTimeEpoch(), frameDate).getDays();
                        incrementsSinceStart = Days.daysBetween(startDate, frameDate).getDays();
                    }
                    if (timeIncrement.equals(ChronoUnit.MONTHS)) {
                        incrementsSinceEpoch = Months.monthsBetween(dataset.getTimeEpoch(), frameDate).getMonths();
                        incrementsSinceStart = Months.monthsBetween(startDate, frameDate).getMonths();
                    }

                    // Skip some frames (if needed)
                    // NOTE: Skipped frames were chosen to highlight different scenarios, verified in tests.
                    boolean skipTemp = false;
                    boolean skipWind = false;
                    boolean skipCurrent = false;
                    if (missingData) {
                        if (hourIndex == startTimeIndex + 2 || hourIndex == startTimeIndex + 3) {
                            continue;
                        }

                        if (hourIndex == startTimeIndex + 5) {
                            skipTemp = true;
                        }
                        if (hourIndex == startTimeIndex + 1) {
                            skipWind = true;
                        }
                        if (hourIndex == startTimeIndex + 8 || hourIndex == startTimeIndex + 9) {
                            skipCurrent = true;
                        }
                    }

                    // Set data for NetCDFTimeVariable

                    // Wind
                    if (!skipWind) {
                        double windUValue = Generator.drawLinearGradient(random, lat, lon - hourIndex, -10, -8, 100, 70, 0);
                        double windVValue = Generator.drawLinearGradient(random, lat - hourIndex, lon, 2, 17, 50, -20, 0);
                        wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                        wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);
                    }

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        if (!skipTemp) {
                            tempRandomVar.addDataPoint(lat, lon, frameDate, depth, (random.nextDouble() - 0.5) * 50);
                            tempTimeIncrementsSinceStartVar.addDataPoint(lat, lon, frameDate, depth, incrementsSinceStart);
                            tempTimeIncrementsSinceEpochVar.addDataPoint(lat, lon, frameDate, depth, incrementsSinceEpoch);
                            tempDepthVar.addDataPoint(lat, lon, frameDate, depth, depth);
                            tempLatVar.addDataPoint(lat, lon, frameDate, depth, lat);
                            tempLonVar.addDataPoint(lat, lon, frameDate, depth, lon);
                        }

                        // Current
                        if (!skipCurrent) {
                            double currentUValue = Generator.drawRadialGradient(random, lat - (hourIndex / 4.0f), lon + (hourIndex / 4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            double currentVValue = Generator.drawRadialGradient(random, lat + (hourIndex / 4.0f), lon + (hourIndex / 4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                            vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                        }
                    }

                }
            }
        }

        netCDFGenerator.generate(file, dataset);

        return file;
    }

    /**
     * {@code Factory} for instantiating a {@link FrameDatesGenerator} specialisation based on the
     * specified {@code timeIncrement}.
     */
    static protected class FrameDatesGeneratorFactory {

        static protected FrameDatesGenerator[] SUPPORTED_FRAME_DATES_GENERATORS = new FrameDatesGenerator[]{
            new HourlyFrameDatesGenerator(),
            new DailyFrameDatesGenerator(),
            new MonhtlyFrameDatesGenerator()
        };

        static public FrameDatesGenerator make(ChronoUnit timeIncrement) {
            for (FrameDatesGenerator frameDatesGenerator : SUPPORTED_FRAME_DATES_GENERATORS) {
                if (frameDatesGenerator.supports(timeIncrement)) {
                    return frameDatesGenerator;
                }
            }
            throw new RuntimeException("Factory does not support time increment \"" +
                timeIncrement.name() + "\".");
        }
    }

    /**
     * Interface for classes that generate the list of frame dates based on the {@code startDate},
     * {@code endDate}, and {@code timeIncrement}.
     */
    static protected interface FrameDatesGenerator {

        /**
         * Identifies if the implementation class supports the {@code timeIncrement}.
         */
        public boolean supports(ChronoUnit timeIncrement);

        /**
         * Generates the list of frame dates.
         */
        public List<DateTime> generate(DateTime startDate, DateTime endDate);
    }

    /**
     * Base implementation of the {@link FrameDatesGenerator} interface.
     */
    abstract static protected class AbstractFrameDatesGenerator implements FrameDatesGenerator {

        /**
         * Base implementation of the {@link #generate(DateTime, DateTime)} method for all
         * specialisations of {@link FrameDatesGenerator} interface. This method invokes the
         * {@link #increment(DateTime)} template method to perform the increment calculation.
         */
        public List<DateTime> generate(DateTime startDate, DateTime endDate) {

            final List<DateTime> frameDates = new ArrayList<>();
            DateTime date = startDate;
            while (date.isBefore(endDate)) {
                frameDates.add(date);
                date = this.increment(date);
            }

            return frameDates;
        }

        /**
         * Template method for incrementing the {@code date} by the appropriate amount based on
         * the specialisation of {@link AbstractFrameDatesGenerator}.
         */
        abstract protected DateTime increment(DateTime date);

    }

    /**
     * Specialisation of {@link AbstractFrameDatesGenerator} to support {@code HOURLY} frame dates.
     */
    static protected class HourlyFrameDatesGenerator extends AbstractFrameDatesGenerator {

        public boolean supports(ChronoUnit timeIncrement) {
            return timeIncrement.equals(ChronoUnit.HOURS);
        }

        public DateTime increment(DateTime date) {
            return date.plusHours(1);
        }

    }

    /**
     * Specialisation of {@link AbstractFrameDatesGenerator} to support {@code DAILY} frame dates.
     */
    static protected class DailyFrameDatesGenerator extends AbstractFrameDatesGenerator {

        public boolean supports(ChronoUnit timeIncrement) {
            return timeIncrement.equals(ChronoUnit.DAYS);
        }

        public DateTime increment(DateTime date) {
            return date.plusDays(1);
        }

    }

    /**
     * Specialisation of {@link AbstractFrameDatesGenerator} to support {@code MONTHLY} frame dates.
     */
    static protected class MonhtlyFrameDatesGenerator extends AbstractFrameDatesGenerator {

        public boolean supports(ChronoUnit timeIncrement) {
            return timeIncrement.equals(ChronoUnit.MONTHS);
        }

        public DateTime increment(DateTime date) {
            return date.plusMonths(1);
        }

    }

}
