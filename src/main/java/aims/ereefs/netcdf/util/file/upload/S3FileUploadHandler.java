package aims.ereefs.netcdf.util.file.upload;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.aws.s3.manager.UploadManager;
import com.amazonaws.services.s3.AmazonS3URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

/**
 * {@link FileUploadHandler} implementation that supports uploading files to {@code AWS S3}.
 *
 * @author Aaron Smith
 */
public class S3FileUploadHandler extends AbstractFileUploadHandler implements FileUploadHandler {

    /**
     * Returns {@code true} if the {@code url} starts with {@code s3:}.
     */
    @Override
    public boolean supports(String url) {
        if ((url != null) && (url.startsWith("s3:"))) {
            logger.debug("Supported: " + url);
            return true;
        }
        return false;
    }

    /**
     * Upload the file to {@code AWS S3}.
     */
    @Override
    public void upload(String localFilename, String url) {

        // Find the local file.
        final File localFile = new File(localFilename);

        logger.debug("Publish starting.");
        try {

            // Build the URI to the remote file.
            logger.debug("url: " + url);
            logger.debug("URI: " + (new URI(url)));
            AmazonS3URI uri = new AmazonS3URI(new URI(url));
            logger.debug("AmazonS3URI: " + uri);

            // Publish the file.
            try (S3Client s3Client = new S3Client()) {
                UploadManager.upload(
                    s3Client,
                    localFile,
                    uri
                );
                apmFileUploadCount.labels("s3").inc();
                apmFileUploadBytes.labels("s3").inc(localFile.length());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish: " + localFile.toPath().toString() +
                " -> " + url, e);
        }
        logger.debug("Upload complete.");
    }

}
