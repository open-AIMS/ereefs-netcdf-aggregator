package aims.ereefs.netcdf.util.file.upload;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * {@link FileUploadHandler} implementation that supports uploading files to a locally addressable
 * location.
 *
 * @author Aaron Smith
 */
public class LocalFileUploadHandler extends AbstractFileUploadHandler implements FileUploadHandler {

    /**
     * Returns {@code true} if the {@code url} starts with {@code file:}.
     */
    @Override
    public boolean supports(String url) {
        if ((url != null) && (url.startsWith("file:"))) {
            logger.debug("Supported: " + url);
            return true;
        }
        return false;
    }

    /**
     * Upload the file to the local location.
     */
    @Override
    public void upload(String localFilename, String url) {

        // Prepare for the remote file.
        final String remoteFilename = url.substring("file:".length());
        final File remoteFile = new File(remoteFilename);
        remoteFile.mkdirs();

        // Find the local file.
        final File localFile = new File(localFilename);
        if (!localFile.exists()) {
            throw new RuntimeException("Local file does not exist.");
        }

        // Copy the file.
        logger.debug("Upload starting.");
        logger.debug("localFile: " + localFile.toPath());
        logger.debug("remoteFile: " + remoteFile.toPath());
        try {
            Files.copy(
                localFile.toPath(),
                remoteFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
            apmFileUploadCount.labels("file").inc();
            apmFileUploadBytes.labels("file").inc(localFile.length());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload: " + localFile.toPath().toString() +
                " -> " + remoteFile.toPath().toString(), e);
        }
        logger.debug("Upload complete.");
    }

}
