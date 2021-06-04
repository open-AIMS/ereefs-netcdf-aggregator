package aims.ereefs.netcdf.input;


import aims.ereefs.netcdf.util.file.cache.FileCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

/**
 * {@code Proxy cache} for static input files defined by a
 * {@link NcAggregateProductDefinition.StaticFile} in
 * {@link NcAggregateProductDefinition#getStaticFiles()}. Actual retrieval and caching is delegated
 * to {@link #fileCache}.
 *
 * @author Aaron Smith
 */
abstract public class AbstractStaticFileCache<T> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cached reference to the centralised {@link FileCache} for downloading and caching static
     * files.
     */
    protected FileCache fileCache;

    /**
     * Cache of all input definitions from {@link NcAggregateProductDefinition#getStaticFiles()}.
     */
    protected NcAggregateProductDefinition.StaticFile[] staticFiles;

    /**
     * Constructor to capture the parameters.
     */
    public AbstractStaticFileCache(FileCache fileCache,
                                   NcAggregateProductDefinition.StaticFile[] staticFiles) {
        this.fileCache = fileCache;
        this.staticFiles = staticFiles;
    }

    /**
     * Retrieves the specified file.
     *
     * @param id the unique identifier for the file.
     */
    public T retrieve(String id) {
        this.logger.trace("debug: " + id);

        // Identify the URL for the file.
        NcAggregateProductDefinition.StaticFile staticFile = Arrays.stream(this.staticFiles)
            .filter(defn -> defn.getId().equalsIgnoreCase(id))
            .findFirst()
            .orElse(null);
        if (staticFile == null) {
            throw new RuntimeException("Definition not found for static file \"" + id + "\".");
        }

        // Retrieve the file from the central cache.
        File localFile = this.fileCache.retrieve(staticFile.getUrl());

        // Verify that the file was downloaded.
        if ((localFile == null) || !localFile.exists()) {
            throw new RuntimeException(
                "Failed to download input file \"" + staticFile.getUrl() + "\".");
        }
        if ((localFile != null) && (localFile.exists())) {

            // Load the file.
            return this.load(staticFile, localFile);

        }

        throw new RuntimeException("Unable to retrieve file for \"" + id + "\".");
    }

    /**
     * Template method for loading/parsing the input file.
     */
    abstract protected T load(NcAggregateProductDefinition.StaticFile staticFile,
                              File localFile);

}