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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@link PreProcessingTask} implementation for populating the
 * {@link ApplicationContext#getFromCache(String) cache} with one or more linking {@code zoneId} to
 * one or more {@code thresholds}.
 *
 * @author Aaron Smith
 */
public class ThresholdCachePopulatorTask implements PreProcessingTask {

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

    public ThresholdCachePopulatorTask(String tempPathname) {
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
            String fieldNames = fileNode.get(FIELD_NAMES_PROPERTY).asText();
            logger.debug("file: " + bindName);

            // Download the file to the local temp directory using a randomly generated filename.
            final String filename = tempPathname + UUID.randomUUID().toString() + ".geojson";
            final File downloadedFile = FileDownloadManager.download(url, filename);
            if ((downloadedFile != null) && (downloadedFile.exists())) {

                // Download successful.
                // Parse the file.
                final SimpleDataset simpleDataset = CsvDatasetParser.parse(downloadedFile, fieldNames.split(","));

                // Build the map, looping through each entry in the CSV dataset.
                Map<String, Double[]> thresholdMap = new HashMap<>();
                for (String[] row : simpleDataset.getData()) {
                    final String zoneId = row[0];
                    final Double[] thresholds = new Double[row.length - 1];
                    for (int thresholdIndex = 1; thresholdIndex < row.length; thresholdIndex++) {
                        Double threshold = Double.valueOf(row[thresholdIndex]);
                        thresholds[thresholdIndex - 1] = threshold;
                    }
                    thresholdMap.put(zoneId, thresholds);
                    logger.debug(zoneId + " : " +
                        Arrays.stream(thresholds).map(t -> t.toString()).collect(Collectors.joining(", ")));
                }

                // Bind to the cache.
                applicationContext.putInCache(
                    bindName,
                    thresholdMap
                );

                // Delete the downloaded file.
                FileDownloadManager.delete(url, downloadedFile);

            } else {
                throw new RuntimeException("Failed to load the GeoJson file \"" + bindName + "\".");
            }
        }

    }

}
