package aims.ereefs.netcdf.input.netcdf;


import aims.ereefs.netcdf.util.file.cache.FileCache;
import au.gov.aims.ereefs.pojo.metadata.Metadata;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.metadata.NetCDFMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.util.CancelTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link InputDataset}-specific {@code proxy cache} of the {@link #fileCache central cache}.
 * Datasets are requested via their unique {@code metadataId}, which this {@code cache} uses to
 * retrieve the {@link Metadata} record via the {@link MetadataDao} interface to identify the
 * location of the dataset to be retrieved. Actual retrieval and caching is delegated to
 * {@link #fileCache}.
 *
 * @author Aaron Smith
 */
public class InputDatasetCache {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected MetadataDao metadataDao;
    protected List<Double> specifiedDepths = new ArrayList<>();

    /**
     * A map binding the {@code MetadataId} of every dataset referenced in the Task to the
     * corresponding {@code InputId} from the Product.
     */
    protected Map<String, List<String>> datasetMetadataIdsByInputIdMap;

    /**
     * Cached reference to the centralised {@link FileCache} for downloading and caching input
     * datasets.
     */
    protected FileCache fileCache;

    /**
     * Historical list of dataset {@code IDs} used to ensure the {@link InputDatasetBuilder} logs
     * the dataset depths only once.
     */
    protected List<String> historicalIds = new ArrayList<>();

    /**
     * Constructor to capture the parameters.
     */
    public InputDatasetCache(FileCache fileCache,
                             MetadataDao metdataDao,
                             double[] specifiedDepths,
                             Map<String, List<String>> datasetMetadataIdsByInputIdMap) {
        this.fileCache = fileCache;
        this.metadataDao = metdataDao;
        this.datasetMetadataIdsByInputIdMap = datasetMetadataIdsByInputIdMap;

        // Convert the depths from String to Double.
        if (specifiedDepths != null) {
            for (int index = 0; index < specifiedDepths.length; index++) {
                this.specifiedDepths.add(specifiedDepths[index]);
            }
        }
    }

    /**
     * Retrieves the specified {@link InputDataset}.
     *
     * @param metadataId the unique identifier for the dataset.
     */
    public InputDataset retrieve(String metadataId) {

        // Retrieve the metadata record from the database.
        final Metadata retrievedMetadata = this.metadataDao.getById(metadataId);
        if (retrievedMetadata == null) {
            throw new RuntimeException("Metadata record not found for " + metadataId);
        }

        // Typecast to NetCDFMetadata, failing immediately if wrong type.
        final NetCDFMetadata metadata = (NetCDFMetadata) retrievedMetadata;

        // Retrieve the file from the cache.
        final String url = metadata.getFileURI();
        File localFile = this.fileCache.retrieve(url, metadata.getChecksum());

        // Stop processing if the file was not downloaded, because we don't know what
        // impact a missing input file will have.
        if ((localFile == null) || !localFile.exists()) {
            throw new RuntimeException(
                "Failed to download input file \"" + metadata.getFileURI() + "\".");
        } else {

            // Open the file as a NetcdfDataset and wrap in an InputDataset.
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Opening dataset: " + metadataId);
            }

            // Open the NetCDF dataset.
            NetcdfDataset.disableNetcdfFileCache();
            NetcdfDataset dataset = null;
            try {
                dataset = NetcdfDataset.openDataset(

                    // location
                    localFile.getAbsolutePath(),

                    // enhance
                    true,

                    // cancelTask
                    (CancelTask) null
                );
                NetcdfDataset.disableNetcdfFileCache();
            } catch (IOException e) {
                String msg = "Failed to open the dataset \"" + metadataId + "\".";
                this.logger.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            // Should the depths be listed when the dataset is built?
            boolean showDepths = !this.historicalIds.contains(metadataId);
            if (showDepths) {
                this.historicalIds.add(metadataId);
            }

            // Wrap the NetCDF dataset as an InputDataset for additional functionality.
            return InputDatasetBuilder.build(
                dataset,
                this.specifiedDepths,
                showDepths
            );

        }

    }

    /**
     * Convenience method that returns an {@link InputDataset} for the specified input source for
     * reference purposes. This method retrieves the first dataset for the corresponding
     * {@code InputId} in {@link #datasetMetadataIdsByInputIdMap}.
     */
    public InputDataset getReferenceDataset(String inputId) {
        final List<String> metadataIds = this.datasetMetadataIdsByInputIdMap.get(inputId);
        if (metadataIds != null && metadataIds.size() > 0) {
            return this.retrieve(metadataIds.get(0));
        } else {
            return null;
        }
    }

    /**
     * Convenience method that returns an {@link InputDataset} for reference purposes. This method
     * invokes {@link #getReferenceDataset(String)} with the first {@code InputId} in
     * {@link #datasetMetadataIdsByInputIdMap}.
     */
    public InputDataset getReferenceDataset() {
        final Iterator<String> inputIdIterator = this.datasetMetadataIdsByInputIdMap.keySet().iterator();
        if (inputIdIterator.hasNext()) {
            return this.getReferenceDataset(inputIdIterator.next());
        }
        return null;
    }

}
