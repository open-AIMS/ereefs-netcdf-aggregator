package aims.ereefs.netcdf.outputStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.units.DateUnit;

/**
 * Factory class responsible for instantiating an {@link OutputFileNameGenerator} to use by the app.
 *
 * @author Greg Coleman
 */
public class OutputFilenameGeneratorFactory {

    static private Logger logger = LoggerFactory.getLogger(OutputFilenameGeneratorFactory.class);

    public static OutputFileNameGenerator make(OutputFileStrategy outputFileStrategy,
                                               String filenamePattern,
                                               DateUnit dateUnit) {

        logger.debug("-- start config param list --");
        logger.debug("strategy: " + outputFileStrategy.name());
        logger.debug("pattern: " + filenamePattern);
        logger.debug("-- end config param list --");


        switch (outputFileStrategy) {
            case ONE:
                return new JustOneFilenameGenerator(filenamePattern, dateUnit);
            case DAILY:
                return new DailyFilenameGenerator(filenamePattern, dateUnit);
            case WEEKLY:
                return new WeeklyFilenameGenerator(filenamePattern, dateUnit);
            case MONTHLY:
                return new MonthlyFilenameGenerator(filenamePattern, dateUnit);
            case SEASONAL:
                // Functionality to create Seasonal output files deprecated. Refactoring of this
                // Factory class required additional work to support instantiation of
                // SeasonalFilenameGenerator, however lack of use of the SeasonalFilenameGenerator
                // resulted in the necessary refactoring work to be deferred indefinitely.
                throw new UnsupportedOperationException("Support for Seasonal output files deprecated.");
            case ANNUAL:
                return new AnnualFilenameGenerator(filenamePattern, dateUnit);
            default:
                throw new RuntimeException("Output filename strategy not supported.");
        }
    }
}
