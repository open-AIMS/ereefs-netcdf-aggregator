package aims.ereefs.netcdf.regrid;


import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.tasks.PreProcessingTask;
import aims.ereefs.netcdf.util.file.download.FileDownloadManager;
import aims.ereefs.netcdf.util.file.upload.FileUploadManager;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Variable;

import java.io.File;
import java.util.UUID;

/**
 * {@link PreProcessingTask} implementation for populating the
 * {@link ApplicationContext#getFromCache(String) cache} with one or more {@link RegularGridMapper}s
 * specified in the Product Definition. If a {@code url} is specified and the referenced file
 * exists, the {@link RegularGridMapper} will be deserialised from that file and added to the
 * cache. If the file does not exist, the {@link RegularGridMapper} will be generated and then
 * serialised to the {@code url} before being added to the cache.
 *
 * @author Aaron Smith
 */
public class RegularGridMapperCachePopulatorTask implements PreProcessingTask {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constants identifying the properties in the JSON configuration.
     */
    static final protected String FILES_PROPERTY = "files";
    static final protected String URL_PROPERTY = "url";
    static final protected String BIND_NAME_PROPERTY = "bindName";
    static final protected String RESOLUTION_PROPERTY = "resolution";

    /**
     * Constant identifying the default value for {@code resolution}.
     */
    private static final double DEFAULT_RESOLUTION = 0.03;

    /**
     * Cached reference to the location where files will be downloaded to.
     */
    protected String tempPathname;

    public RegularGridMapperCachePopulatorTask(String tempPathname) {
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
            double resolution = DEFAULT_RESOLUTION;
            if (fileNode.has(RESOLUTION_PROPERTY)) {
                resolution = fileNode.get(RESOLUTION_PROPERTY).asDouble();
                if (resolution == 0.0) {
                    resolution = DEFAULT_RESOLUTION;
                }
            }
            logger.debug("file: " + bindName);

            // Download the file to the local temp directory using a randomly generated filename.
            final String filename = tempPathname + UUID.randomUUID().toString() + ".ser";
            File downloadedFile = null;
            try {
                downloadedFile = FileDownloadManager.download(url, filename);
            } catch(Exception ignore) {
                // Ignore any exception downloading the RegularGridMapper cache file so it can be
                // (re-)generated.
                logger.warn("Failed to download RegularGridMapper cache file. Generating it now!");
            }
            if ((downloadedFile != null) && (downloadedFile.exists())) {

                // Download successful, to parse the file and add it to the cache.
                applicationContext.putInCache(
                    bindName,
                    CacheReader.readAsRegularGridMapper(downloadedFile.getAbsolutePath())
                );

                // Delete the downloaded file.
                FileDownloadManager.delete(url, downloadedFile);

            } else {

                // File could not be downloaded, so build and publish.
                final InputDataset referenceDataset =
                    applicationContext.getInputDatasetCache().getReferenceDataset();
                logger.debug("Reading latitude variable.");
                Variable latitudeVariable = null;
                try {
                    latitudeVariable = referenceDataset.getLatitudeVariable();
                    logger.debug("Latitude variable size: " + latitudeVariable.getSize());
                } catch (Throwable throwable) {
                    throw new RuntimeException(
                        "Failed to get reference to latitude variable from reference dataset.",
                        throwable
                    );
                }
                logger.debug("Reading longitude variable.");
                Variable longitudeVariable = null;
                try {
                    longitudeVariable = referenceDataset.getLongitudeVariable();
                    logger.debug("Longitude variable size: " + longitudeVariable.getSize());
                } catch (Throwable throwable) {
                    throw new RuntimeException(
                        "Failed to reference to longitude variable from reference dataset.",
                        throwable
                    );
                }

                logger.debug("resolution: " + resolution);

                RegularGridMapper regularGridMapper = null;
                try {
                    regularGridMapper = RegularGridMapperBuilder.make(
                        latitudeVariable.read(),
                        longitudeVariable.read(),
                        resolution,
                        null
                    );

                    applicationContext.putInCache(
                        bindName,
                        regularGridMapper
                    );
                } catch (Throwable throwable) {
                    throw new RuntimeException(
                        "Failed to instantiate a RegularGridMapper.",
                        throwable
                    );
                }

                // Persist the RegularGridMapper to the local file, and then archive it.
                File localFile = new File(tempPathname + UUID.randomUUID().toString() + ".ser");
                localFile.getParentFile().mkdirs();
                CacheWriter.write(regularGridMapper, localFile.getAbsolutePath());
                FileUploadManager.upload(
                    localFile.getAbsolutePath(),
                    url
                );

                // Delete the local file.
                localFile.delete();

            }
        }

    }

}