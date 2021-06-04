package aims.ereefs.netcdf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * Class responsible for identifying and initialising the temporary directory for the application.
 * This class creates the directory (and parents) if they don't exist (expected behaviour), and
 * deletes any existing files if they do exist (unexpected behaviour that is handled).
 *
 * @author Aaron Smith
 */
public class TempDirectoryInitialiser {

    static protected Logger logger = LoggerFactory.getLogger(TempDirectoryInitialiser.class);

    final static public String APP_NAME = "ereefs-netcdf-aggregator";

    static public String initialise() {

        // Build the name of the temp directory.
        final String pathname = System.getProperty("java.io.tmpdir") + File.separator + APP_NAME +
            File.separator + UUID.randomUUID().toString() + File.separator;
        logger.debug("pathname: " + pathname);
        File path = new File(pathname);

        // Clear the directory if it already exists.
        if (path.exists()) {
            logger.debug("Temp directory exists. Deleting any files.");
            for (File file : path.listFiles()) {
                logger.debug("Delete: " + file.getName());
                file.delete();
            }
        } else {
            logger.debug("Temp directory does not exist. Creating it.");
            path.mkdirs();
        }

        return pathname;
    }
}