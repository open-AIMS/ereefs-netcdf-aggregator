package aims.ereefs.netcdf.metadata.populate;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.ApplicationContextBuilder;
import aims.ereefs.netcdf.OperationModeExecutor;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Implements the {@link OperationModeExecutor} interface for populating the database with the
 * {@code Metadata} of specified NetCDF files from the command-line.
 *
 * @author Aaron Smith
 */
public class PopulateMetadataOperationModeExecutor implements OperationModeExecutor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Return {@code true} if {@code args} contains {@code --populate-metadata}.
     */
    @Override
    public boolean supports(String[] args) {
        return Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("--populate-metadata"));
    }

    /**
     * Coordinate the metadata population operation.
     */
    @Override
    public void execute(String[] args) {
        logger.debug("Executing");

        // Capture the command line arguments.
        String inputPath = null;
        String definitionId = null;
        for (final String arg : args) {

            if (arg.startsWith("--input=")) {
                inputPath = arg.substring("--input=".length());
                logger.debug("Found: \"inputPath\" : " + inputPath);
            }

            if (arg.startsWith("--definitionId=")) {
                definitionId = arg.substring("--definitionId=".length());
                logger.debug("Found: \"definitionId\" : " + definitionId);
            }

        }
        if (inputPath == null) {
            throw new RuntimeException("Input path not specified. Use \"--input=<input path>\"");
        }
        if (definitionId == null) {
            throw new RuntimeException("Definition Id not specified. Use \"--definitionId=<id>\"");
        }

        // Build the ApplicationContext, containing references to properties, helpers and utilities
        // that are static throughout the entire life of the application, but need to be accessed
        // by multiple parts of the application.
        final ApplicationContext applicationContext = new ApplicationContext("populate");
        ApplicationContextBuilder.populateDao(applicationContext);

        // Identify the files to process.
        File path = new File(inputPath);
        if (!path.exists()) {
            throw new RuntimeException("\"" + inputPath + "\" does not exist.");
        }
        File[] files = path.listFiles();
        logger.debug("\"" + inputPath + "\" has " + files.length + " files.");

        // Process each file.
        for (File file : files) {
            logger.debug(file.getAbsolutePath());
            String uriStr = "file:" + file.getPath().replace("\\", "/");
            try {

                // Use the library to populate the Metadata object.
                NetCDFMetadataBean metadata = NetCDFMetadataBean.create(
                    definitionId,
                    file.getName(),
                    new URI(uriStr),
                    file,
                    DateTime.now().getMillis()
                );

                // Persist the Metadata object.
                applicationContext.getMetadataDao().persist(metadata.toJSON());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
