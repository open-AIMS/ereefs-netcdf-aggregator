package aims.ereefs.netcdf.util.file.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility for downloading files from a supported file storage mechanism. This utility currently
 * supports local drives and {@code AWS S3}.
 *
 * @author Aaron Smith
 */
public class FileDownloadManager {

    static protected Logger logger = LoggerFactory.getLogger(FileDownloadManager.class);

    static final protected List<FileDownloadHandler> SUPPORTED_FILE_DOWNLOAD_HANDLERS =
        new ArrayList<FileDownloadHandler>() {{
            add(new LocalFileDownloadHandler());
            add(new S3FileDownloadHandler());
        }};

    static protected final int MAX_RETRY_COUNT = 3;

    /**
     * Retrieve the size of the file.
     *
     * @param url the location of the file. This value includes a prefix that identifies the
     *            storage mechanism.
     */
    static public long getFileSize(String url) {

        final Iterator<FileDownloadHandler> iterator = SUPPORTED_FILE_DOWNLOAD_HANDLERS.iterator();
        while (iterator.hasNext()) {
            final FileDownloadHandler handler = iterator.next();
            if (handler.supports(url)) {
                return handler.getFileSize(url);
            }
        }

        throw new RuntimeException("Storage mechanism not supported.");
    }

    /**
     * Download the file from the file storage mechanism.
     *
     * @param url       the location of the file to download. This value is expected to identify the
     *                  file storage mechanism as a prefix. Supported values are: {@code file:} and
     *                  {@code s3:}.
     * @param localPath the local location to store the file after retrieval.
     * @return reference to the local {@code File}. This value will be {@code null} if the remote
     * file does not exist.
     */
    static public File download(String url,
                                String localPath) {

        logger.debug("Download file \"" + url + "\" -> \"" + localPath + "\".");

        final Iterator<FileDownloadHandler> iterator = SUPPORTED_FILE_DOWNLOAD_HANDLERS.iterator();
        while (iterator.hasNext()) {
            final FileDownloadHandler handler = iterator.next();
            if (handler.supports(url)) {
                int retryCounter = 1;
                while (retryCounter <= MAX_RETRY_COUNT) {
                    try {
                        if (retryCounter > 1) {
                            logger.debug("Retry " + retryCounter + " of " + MAX_RETRY_COUNT);
                        }
                        return handler.download(url, localPath);
                    } catch (Exception e) {
                        logger.error("Retrieval failed. Retrying.", e);
                        retryCounter++;
                    }
                }
                throw new RuntimeException("Retry count exceeded.");
            }
        }

        throw new RuntimeException("Storage mechanism not supported.");

    }

    /**
     * Delete a file that was downloaded from a storage mechanism. Use this method instead of
     * deleting the file directly as this abstracts the download mechanism used, allowing for
     * locally sourced files to {@code NOT} be deleted.
     *
     * @param url            the location of the file to download. This value is expected to identify the
     *                       file storage mechanism as a prefix. Supported values are: {@code file:} and
     *                       {@code s3:}.
     * @param downloadedFile reference to the file downloaded by the
     *                       {@link #download(String, String)} method.
     * @return {@code true} if the file was deleted, {@code false} otherwise.
     */
    static public boolean delete(String url,
                                 File downloadedFile) {

        final Iterator<FileDownloadHandler> iterator = SUPPORTED_FILE_DOWNLOAD_HANDLERS.iterator();
        while (iterator.hasNext()) {
            final FileDownloadHandler handler = iterator.next();
            if (handler.supports(url)) {
                return handler.delete(downloadedFile);
            }
        }

        throw new RuntimeException("Storage mechanism not supported.");

    }

}
