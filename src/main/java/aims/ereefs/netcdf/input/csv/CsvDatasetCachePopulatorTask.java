package aims.ereefs.netcdf.input.csv;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.input.SimpleDataset;
import aims.ereefs.netcdf.tasks.PreProcessingTask;
import aims.ereefs.netcdf.util.file.download.FileDownloadManager;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * {@link PreProcessingTask} implementation for populating the
 * {@link ApplicationContext#getFromCache(String) cache} with one or more {@link SimpleDataset}
 * objects specified in the {@code ProductDefinition}.
 *
 * @author Aaron Smith
 */
public class CsvDatasetCachePopulatorTask implements PreProcessingTask {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constants identifying the properties in the JSON configuration.
     */
    static final protected String FILES_PROPERTY = "files";
    static final protected String URL_PROPERTY = "url";
    static final protected String BIND_NAME_PROPERTY = "bindName";
    static final protected String FIELD_NAMES_PROPERTY = "fieldNames";

    /**
     * Cached reference to the location where files will be downloaded to.
     */
    protected String tempPathname;

    public CsvDatasetCachePopulatorTask(String tempPathname) {
        this.tempPathname = tempPathname;
    }

    @Override
    public boolean supports(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn) {
        return this.getClass().getSimpleName().equalsIgnoreCase(taskDefn.getType());
    }

    @Override
    public void process(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn,
                        ApplicationContext applicationContext) {

        // Loop through each file defined.
        for (JsonNode fileNode : taskDefn.getJson().get(FILES_PROPERTY)) {
            final String url = fileNode.get(URL_PROPERTY).asText();
            final String bindName = fileNode.get(BIND_NAME_PROPERTY).asText();
            final String fieldNames = fileNode.get(FIELD_NAMES_PROPERTY).asText();
            logger.debug("file: " + bindName);

            // Download the file to the local temp directory using a randomly generated filename.
            final String filename = tempPathname + UUID.randomUUID().toString() + ".geojson";
            final File downloadedFile = FileDownloadManager.download(url, filename);
            if ((downloadedFile != null) && (downloadedFile.exists())) {

                // Download successful, parse the file, instantiate the dataset, and bind to the
                // cache.
                applicationContext.putInCache(
                    bindName,
                    CsvDatasetParser.parse(downloadedFile, fieldNames.split(","))
                );

                // Delete the downloaded file.
                FileDownloadManager.delete(url, downloadedFile);

            } else {
                throw new RuntimeException("Failed to load the GeoJson file \"" + bindName + "\".");
            }
        }

    }

}
