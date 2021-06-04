package aims.ereefs.netcdf.util.file.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility for uploading files to a supported file storage mechanism. This utility currently
 * supports local drives and {@code AWS S3}.
 *
 * @author Aaron Smith
 */
public class FileUploadManager {

    static protected Logger logger = LoggerFactory.getLogger(FileUploadManager.class);

    static final protected List<FileUploadHandler> SUPPORTED_FILE_UPLOAD_HANDLERS =
        new ArrayList<FileUploadHandler>() {{
            add(new LocalFileUploadHandler());
            add(new S3FileUploadHandler());
        }};

    static protected final int MAX_RETRY_COUNT = 3;

    /**
     * Upload the file to the file storage mechanism.
     *
     * @param localFilename the local location of the file to publish.
     * @param url           the remote location of the file to publish. This value is expected to
     *                      identify the file storage mechanism as a prefix. Supported values are:
     *                      {@code file:} and {@code s3:}.
     */
    static public void upload(String localFilename,
                              String url) {

        logger.debug("Upload file \"" + localFilename + "\" -> \"" + url + "\".");

        boolean isHandled = false;
        final Iterator<FileUploadHandler> iterator = SUPPORTED_FILE_UPLOAD_HANDLERS.iterator();
        while (iterator.hasNext() && !isHandled) {
            final FileUploadHandler handler = iterator.next();
            if (handler.supports(url)) {
                int retryCounter = 1;
                while (retryCounter <= MAX_RETRY_COUNT && !isHandled) {
                    try {
                        if (retryCounter > 1) {
                            logger.debug("Retry " + retryCounter + " of " + MAX_RETRY_COUNT);
                        }
                        handler.upload(localFilename, url);
                        isHandled = true;
                    } catch (Exception e) {
                        logger.error("Publish failed. Retrying.", e);
                        retryCounter++;
                    }
                }
                if (!isHandled) {
                    throw new RuntimeException("Retry count exceeded.");
                }
            }
        }

        if (!isHandled) {
            throw new RuntimeException("Storage mechanism not supported.");
        }

    }

}
