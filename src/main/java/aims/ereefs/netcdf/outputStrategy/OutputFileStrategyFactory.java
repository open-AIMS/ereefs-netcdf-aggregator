package aims.ereefs.netcdf.outputStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Factory</code> responsible for interpreting the config file to determine which
 * {@link OutputFileStrategy} to use. Supported values are:
 *
 * - 'd' : One output file per day.
 * - 'm' : One output file per month
 * - 'o' : Only one output file.
 * - 's' : One output file per season.
 * - 'a' : One output file per year (annual).
 * - 'w' : One output file per week.
 *
 * @author Aaron Smith
 */
public class OutputFileStrategyFactory {

    static private Logger logger = LoggerFactory.getLogger(OutputFileStrategyFactory.class);

    /**
     * Interpret the specified <code>id</code> to return the corresponding
     * {@link OutputFileStrategy}.
     *
     * @param id value to be interpreted.
     * @return the corresponding {@link OutputFileStrategy}, with {@link OutputFileStrategy#INPUT}
     * used for default.
     */
    static public OutputFileStrategy make(String id) {

        logger.info("id: " + id);

        // Default.
        OutputFileStrategy outputFileStrategy = OutputFileStrategy.INPUT;

        if (id.toLowerCase().startsWith("d")) {
            outputFileStrategy = OutputFileStrategy.DAILY;
        }
        if (id.toLowerCase().startsWith("m")) {
            outputFileStrategy = OutputFileStrategy.MONTHLY;
        }
        if (id.toLowerCase().startsWith("o")) {
            outputFileStrategy = OutputFileStrategy.ONE;
        }
        if (id.toLowerCase().startsWith("s")) {
            outputFileStrategy = OutputFileStrategy.SEASONAL;
        }
        if (id.toLowerCase().startsWith("a")) {
            outputFileStrategy = OutputFileStrategy.ANNUAL;
        }
        if (id.toLowerCase().startsWith("w")) {
            outputFileStrategy = OutputFileStrategy.WEEKLY;
        }

        logger.info("strategy: " + outputFileStrategy);

        return outputFileStrategy;

    }

}
