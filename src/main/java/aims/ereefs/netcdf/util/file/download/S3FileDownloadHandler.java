package aims.ereefs.netcdf.util.file.download;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.aws.s3.manager.DownloadManager;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.net.URI;

/**
 * {@link FileDownloadHandler} implementation that supports uploading files to {@code AWS S3}.
 *
 * @author Aaron Smith
 */
public class S3FileDownloadHandler extends AbstractFileDownloadHandler {

    /**
     * Constant identifying the file download scheme supported by this class.
     */
    static protected String SCHEME = "s3";

    /**
     * Constructor to initialise the {@link #scheme}.
     */
    public S3FileDownloadHandler() {
        super();
        this.scheme = S3FileDownloadHandler.SCHEME;
    }

    /**
     * Returns {@code true} if the {@code url} starts with {@code s3:}.
     */
    @Override
    public boolean supports(String url) {
        if ((url != null) && (url.startsWith(S3FileDownloadHandler.SCHEME + ":"))) {
            logger.debug("Supported: " + url);
            return true;
        }
        return false;
    }

    @Override
    public long getFileSize(String url) {

        try {

            // Build the URI to the remote file.
            AmazonS3URI s3URI = new AmazonS3URI(new URI(url));

            try (S3Client s3Client = new S3Client()) {
                S3Object s3Object = s3Client.getS3().getObject(s3URI.getBucket(), s3URI.getKey());
                if (s3Object != null) {
                    ObjectMetadata metadata = s3Object.getObjectMetadata();
                    logger.debug("instanceLength: " + metadata.getInstanceLength());
                    logger.debug("contentLength: " + metadata.getContentLength());
                    return metadata.getInstanceLength();
                }
            }
        } catch (Exception e) {
            if ((e instanceof AmazonS3Exception) &&
                e.getMessage().startsWith("The specified key does not exist")) {
                logger.warn("File not found in S3.");
                return -1;
            } else {
                throw new RuntimeException("Failed to get length: " + url, e);
            }
        }

        return -1;
    }

    /**
     * Always delete the file.
     */
    @Override
    public boolean delete(File downloadedFile) {
        return downloadedFile.delete();
    }

    /**
     * Download the file from {@code AWS S3}.
     */
    @Override
    protected File doDownload(String url, String localPath) {

        // Prepare for the local file.
        File path = new File(localPath);
        path.mkdirs();
        int lastSeparatorPos = url.lastIndexOf("/");
        if (lastSeparatorPos == -1) {
            lastSeparatorPos = url.lastIndexOf("\\");
        }
        File localFile = new File(path.getAbsolutePath() + File.separator +
            url.substring(lastSeparatorPos));

        logger.debug("Download starting.");
        try {

            // Build the URI to the remote file.
            AmazonS3URI s3URI = new AmazonS3URI(new URI(url));

            // Retrieve the file.
            try (S3Client s3Client = new S3Client()) {
                DownloadManager.download(
                    s3Client,
                    s3URI,
                    localFile
                );
                apmFileDownloadCount.labels("s3").inc();
                apmFileDownloadBytes.labels("s3").inc(localFile.length());
            }

        } catch (Exception e) {
            if ((e instanceof AmazonS3Exception) &&
                e.getMessage().startsWith("The specified key does not exist")) {
                logger.warn("File not found in S3.");
                return null;
            } else {
                throw new RuntimeException("Failed to retrieve: " + url +
                    " -> " + localFile.toPath().toString(), e);
            }
        }
        logger.debug("Download complete.");

        return localFile;

    }

}
