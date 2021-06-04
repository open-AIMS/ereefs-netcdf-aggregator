package aims.ereefs.netcdf.util.file.cache;

import aims.ereefs.netcdf.util.file.download.FileDownloadManager;
import au.gov.aims.ereefs.Utils;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements a managed file cache that coordinates available disk space, and uses
 * {@link FileDownloadManager} to retrieve files from a supported storage mechanism.
 *
 * @author Aaron Smith
 */
public class FileCache {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Gauge apmFileCacheCount = Gauge.build()
        .name("ncaggregate_file_cache_count")
        .help("Total number of files held in the cache.")
        .register();
    protected static final Gauge apmFileCacheBytes = Gauge.build()
        .name("ncaggregate_file_cache_bytes")
        .help("Total size (bytes) of files in cache.")
        .register();
    protected static final Gauge apmMaxFileCacheSizeBytes = Gauge.build()
        .name("ncaggregate_max_file_cache_size_bytes")
        .help("Maximum size of file cache (bytes).")
        .register();

    /**
     * The local file cache directory.
     */
    protected String cachePath;

    /**
     * Mapping of {@code metadataId} to the location of the file locally.
     */
    protected Map<String, File> cachedFileMap = new TreeMap<>();

    /**
     * The size of the cache, in bytes.
     */
    protected long cacheSize = 0;

    /**
     * The maximum size the cache can grow to, in bytes.
     */
    protected long maxFileCacheSize = 0;

    /**
     * A maintained list that shows the order of access. That is, the greater the index in the list,
     * the more recent the {@code url} was accessed. For example, an index of {@code 1} is the
     * least accessed {@code url.}
     */
    protected List<String> orderOfAccess = new ArrayList<>();

    /**
     * Constructor to capture the parameters.
     *
     * @param cachePath        the directory to use for the file cache.
     * @param maxFileCacheSize the maximum size in GB the file cache can grow to before cleaning up.
     */
    public FileCache(String cachePath,
                     int maxFileCacheSize) {
        this.cachePath = cachePath;
        this.maxFileCacheSize = (long) maxFileCacheSize * 1000000000;
        apmMaxFileCacheSizeBytes.set(this.maxFileCacheSize);
        logger.debug("maxFileCacheSize: " + this.maxFileCacheSize);
    }

    /**
     * Returns the {@code File} reference to the caller. If the file is already in the
     * {@link #cachedFileMap cache}, the reference is simply returned. If not, the size of the
     * requested file is first determined, and if necessary space in the cache is made available,
     * before downloading and caching the file before returning the reference.
     *
     * @param url the location of the file to retrieve. This value is expected to identify the
     *            file storage mechanism as a prefix. Supported values are: {@code file:} and
     *            {@code s3:}.
     * @return reference to the local {@code File}. This value will be {@code null} if the remote
     * file does not exist.
     */
    public File retrieve(String url) {
        return this.retrieve(url, null);
    }

    /**
     * Returns the {@code File} reference to the caller. If the file is already in the
     * {@link #cachedFileMap cache}, the reference is simply returned. If not, the size of the
     * requested file is first determined, and if necessary space in the cache is made available,
     * before downloading and caching the file before returning the reference.
     *
     * @param url      the location of the file to retrieve. This value is expected to identify the
     *                 file storage mechanism as a prefix. Supported values are: {@code file:} and
     *                 {@code s3:}.
     * @param checksum the checksum to verify the contents of the retrieved file. Ignored it
     *                 {@code null}.
     * @return reference to the local {@code File}. This value will be {@code null} if the remote
     * file does not exist.
     */
    public File retrieve(String url, String checksum) {

        // Is the file cached?
        final File localFile = this.cachedFileMap.get(url);
        if (localFile != null) {

            // Mark this url as the most recently accessed.
            this.orderOfAccess.remove(url);
            this.orderOfAccess.add(url);

            return localFile;
        } else {

            // Identify the size of the file to download.
            long fileSize = FileDownloadManager.getFileSize(url);

            // Clear space if required.
            while ((fileSize + this.cacheSize > this.maxFileCacheSize) &&
                !this.cachedFileMap.isEmpty()) {

                // Delete the file that was accessed longest ago.
                final String deleteUrl = this.orderOfAccess.remove(0);
                final File deleteFile = this.cachedFileMap.get(deleteUrl);
                this.cacheSize -= deleteFile.length();
                FileDownloadManager.delete(deleteUrl, deleteFile);
                this.cachedFileMap.remove(deleteUrl);

            }

            // Is there enough space in the cache?
            if (fileSize + this.cacheSize > this.maxFileCacheSize) {
                throw new RuntimeException("Not enough space available to download file. " +
                    "cacheSize: " + this.cacheSize + "; fileSize: " + fileSize + "; url: " + url);
            }

            // Download the file. Up to 3 attempts will be made.
            int retriesRemaining = 3;
            while (retriesRemaining > 0) {
                logger.debug("Download \"" + url + "\" to \"" + this.cachePath + "\".");
                File downloadedFile = FileDownloadManager.download(url, this.cachePath);
                if (downloadedFile != null) {

                    // Validate the downloaded file as required.
                    boolean isValid = true;

                    // Compare the checksum if specified.
                    if (checksum != null) {
                        String generatedChecksum = null;
                        try {
                            logger.debug("Generating checksum.");
                            generatedChecksum = String.format("%s:%s", "MD5", Utils.checksum(downloadedFile, "MD5"));
                            logger.debug("Checksum generated.");
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to generate checksum for downloaded file.", e);
                        }
                        if (!checksum.equalsIgnoreCase(generatedChecksum)) {
                            isValid = false;
                            logger.debug("Checksum comparison failed. Expected : \"" + checksum +
                                "\"; found: \"" + generatedChecksum + "\".");
                        }
                    }

                    // Cache the downloaded file if valid, and return the reference.
                    if (isValid) {
                        this.cachedFileMap.put(url, downloadedFile);
                        this.orderOfAccess.add(url);
                        this.cacheSize += downloadedFile.length();

                        apmFileCacheCount.set(this.cachedFileMap.size());
                        apmFileCacheBytes.set(this.cacheSize);

                        return downloadedFile;

                    }

                }

                // File was not downloaded, or it wasn't valid, so retry.
                retriesRemaining--;
                logger.debug("Download failed. Retries remaining: " + retriesRemaining);

            }
            throw new RuntimeException("Unable to retrieve the specified file. (url: " + url + ")");

        }

    }

}