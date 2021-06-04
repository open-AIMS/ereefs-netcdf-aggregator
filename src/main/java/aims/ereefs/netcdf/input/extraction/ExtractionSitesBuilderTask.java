package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.regrid.Coordinate;
import aims.ereefs.netcdf.tasks.PreProcessingTask;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * {@link PreProcessingTask} implementation for populating the
 * {@link ApplicationContext#getFromCache(String) cache} with site information for extracting data.
 * This class defers to {@link ExtractionSiteListBuilder} to build a list of {@link ExtractionSite}
 * objects, which are then bound to the {@link ApplicationContext#getFromCache(String) cache} with
 * the key specified by {@link #EXTRACTION_SITES_BIND_NAME}.
 *
 * @author Aaron Smith
 */
public class ExtractionSitesBuilderTask implements PreProcessingTask {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constants identifying the properties in the JSON configuration.
     */
    static final protected String IS_RECTILINEAR_GRID_PROPERTY = "isRectilinearGrid";
    static final protected String SITES_PROPERTY = "sites";

    /**
     * Bind name for the list of {@link ExtractionSite}s.
     */
    static final public String EXTRACTION_SITES_BIND_NAME = "extractionSites";

    /**
     * Cached reference to the location where files will be downloaded to.
     */
    protected String tempPathname;

    public ExtractionSitesBuilderTask(String tempPathname) {
        this.tempPathname = tempPathname;
    }

    @Override
    public boolean supports(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn) {
        return this.getClass().getSimpleName().equalsIgnoreCase(taskDefn.getType());
    }

    @Override
    public void process(NcAggregateProductDefinition.PreProcessingTaskDefn taskDefn,
                        ApplicationContext applicationContext) {

        final JsonNode rootNode = taskDefn.getJson();
        final boolean isRectilinearGrid = rootNode.has(IS_RECTILINEAR_GRID_PROPERTY) &&
            rootNode.get(IS_RECTILINEAR_GRID_PROPERTY).asBoolean();

        // Use the reference dataset to build a map of pixel/cell index (with a data array) to the
        // corresponding lat/lon coordinate.
        InputDataset referenceDataset = applicationContext.getInputDatasetCache().getReferenceDataset();
        final Array longitudeArray;
        final Array latitudeArray;
        final Map<Integer, Coordinate> indexToCoordinateMap;
        try {
            latitudeArray = referenceDataset.getLatitudeVariable().read();
            longitudeArray = referenceDataset.getLongitudeVariable().read();
            indexToCoordinateMap = CoordinateMapBuilder.build(
                latitudeArray,
                longitudeArray,
                isRectilinearGrid
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to build coordinate map.", e);
        }

        // Build the list of ExtractionSites.
        final List<ExtractionSite> extractionSiteList = ExtractionSiteListBuilder.build(
            rootNode.get(SITES_PROPERTY),
            indexToCoordinateMap
        );
        applicationContext.putInCache(EXTRACTION_SITES_BIND_NAME, extractionSiteList);
    }

}
