package aims.ereefs.netcdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class that logs the operating environment for the application.
 *
 * @author Aaron Smith
 */
public class EnvironmentLogger {

    private static Logger logger = LoggerFactory.getLogger(EnvironmentLogger.class);

    final static private List<String> SYSTEM_PROPERTIES = Arrays.asList(new String[]{
            "java.runtime.name",
            "java.runtime.version",
            "java.vm.name",
            "java.vm.vendor",
            "java.vm.version",
            "java.io.tmpdir",
            "user.name",
            "user.home",
            "user.dir",
            "user.timezone",
            "os.name"
        }
    );

    public static void dump(String[] args) throws Exception {

        Runtime runtime = Runtime.getRuntime();
        logger.info("Location: " + EnvironmentLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        logger.info("TotalMemory: " + runtime.totalMemory());
        logger.info("FreeMemory: " + runtime.freeMemory());
        logger.info("MaxMemory: " + runtime.maxMemory());

        // Dump all environment variables.
        logger.debug("----- System.getenv() -----");
        final Map<String, String> env = new TreeMap<>(System.getenv());
        for (final String key : env.keySet()) {
            logger.debug(key + " : " + env.get(key));
        }
        logger.debug("---------------------------");

        // Dump only selected system properties.
        logger.debug("----- System.getProperties() -----");
        final Properties properties = System.getProperties();
        for (final String key : SYSTEM_PROPERTIES) {
            logger.debug(key + " : " + properties.getProperty(key));
        }
        logger.debug("----------------------------------");

        // Dump command line arguments.
        logger.debug("----- Command Line Arguments -----");
        for (final String arg : args) {
            logger.debug(arg);
        }
        logger.debug("----------------------------------");

    }

}