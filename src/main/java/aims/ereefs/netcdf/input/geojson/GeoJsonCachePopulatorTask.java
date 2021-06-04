package aims.ereefs.netcdf.input.geojson;


import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.tasks.PreProcessingTask;
import aims.ereefs.netcdf.util.file.ReadUtils;
import aims.ereefs.netcdf.util.file.download.FileDownloadManager;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * {@link PreProcessingTask} implementation for loading and processing one or more GeoJson files,
 * and populating the {@link ApplicationContext#getFromCache(String) cache} with the result(s). A
 * GeoJson file contains polygons that define geographic zones/regions within the map boundaries.
 * This class generates a map binding each cell/location within the map boundaries to the
 * {@code Id} of the zone/region it belongs to, and then binds that map to the
 * {@link ApplicationContext#getFromCache(String) cache}.
 *
 * @author Aaron Smith
 * @see IndexToZoneIdMapBuilder
 */
public class GeoJsonCachePopulatorTask implements PreProcessingTask {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constants identifying the properties in the JSON configuration.
     */
    static final protected String FILES_PROPERTY = "files";
    static final protected String URL_PROPERTY = "url";
    static final protected String BIND_NAME_PROPERTY = "bindName";
    static final protected String IS_RECTILINEAR_GRID_PROPERTY = "isRectilinearGrid";

    /**
     * Cached reference to the location where files will be downloaded to.
     */
    protected String tempPathname;

    public GeoJsonCachePopulatorTask(String tempPathname) {
        this.tempPathname = tempPathname;
    }

    @Override
    public boolean supports(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn) {
        return this.getClass().getSimpleName().equalsIgnoreCase(taskDefn.getType());
    }

    @Override
    public void process(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn,
                        ApplicationContext applicationContext) {

        // Obtain the reference dataset.
        final InputDataset referenceDataset = applicationContext.getInputDatasetCache().getReferenceDataset();

        // Loop through each file defined.
        for (JsonNode fileNode : taskDefn.getJson().get(FILES_PROPERTY)) {
            final String url = fileNode.get(URL_PROPERTY).asText();
            final String bindName = fileNode.get(BIND_NAME_PROPERTY).asText();
            final boolean isRectilinearGrid = fileNode.get(IS_RECTILINEAR_GRID_PROPERTY).asBoolean();
            logger.debug("file: " + bindName);

            // Download the file to the local temp directory using a randomly generated filename.
            final String filename = tempPathname + UUID.randomUUID().toString() + ".geojson";
            final File downloadedFile = FileDownloadManager.download(url, filename);
            if ((downloadedFile != null) && (downloadedFile.exists())) {

                try {
                    // Download successful, to parse the file.
                    final JSONObject json = new JSONObject(ReadUtils.readTextFileAsString(downloadedFile));

                    // Generate the IndexToZoneId map.
                    final List<String> indexToZoneIdMap = IndexToZoneIdMapBuilder.build(
                        referenceDataset,
                        json,
                        Boolean.valueOf(isRectilinearGrid)
                    );

                    // Bind it to the cache.
                    applicationContext.putInCache(
                        bindName,
                        indexToZoneIdMap
                    );
                } catch (Throwable throwable) {
                    throw new RuntimeException("Failed to load the GeoJSON file.", throwable);
                }

                // Delete the downloaded file.
                FileDownloadManager.delete(url, downloadedFile);

            } else {
                throw new RuntimeException("Failed to load the GeoJson file \"" + bindName + "\".");
            }
        }

    }

}
