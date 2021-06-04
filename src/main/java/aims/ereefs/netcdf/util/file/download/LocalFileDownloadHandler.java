package aims.ereefs.netcdf.util.file.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * {@link FileDownloadHandler} implementation that supports downloading files from a local location.
 * Note that files downloaded from a local location should NOT be deleted after use.
 *
 * @author Aaron Smith
 */
public class LocalFileDownloadHandler extends AbstractFileDownloadHandler {

    /**
     * Constant identifying the file download scheme supported by this class.
     */
    static protected String SCHEME = "file";

    /**
     * Constructor to initialise the {@link #scheme}.
     */
    public LocalFileDownloadHandler() {
        super();
        this.scheme = LocalFileDownloadHandler.SCHEME;
    }

    /**
     * Returns {@code true} if the {@code url} starts with {@code file:}.
     */
    @Override
    public boolean supports(String url) {
        if ((url != null) && (url.startsWith(LocalFileDownloadHandler.SCHEME + ":"))) {
            logger.debug("Supported: " + url);
            return true;
        }
        return false;
    }

    /**
     * Retrieve the size of the file.
     */
    @Override
    public long getFileSize(String url) {

        // Find the remote file.
        final String remoteFilename = url.substring("file:".length());
        File remoteFile = new File(remoteFilename);
        if (!remoteFile.exists()) {
            throw new RuntimeException("Remote file does not exist.");
        }

        return remoteFile.length();

    }

    /**
     * Never delete the file.
     */
    @Override
    public boolean delete(File downloadedFile) {
        return false;
    }

    /**
     * No actual file download is performed. The reference to the local file is returned.
     */
    @Override
    protected File doDownload(String url, String localPath) {

        // Find the remote file.
        final String remoteFilename = url.substring("file:".length());
        logger.debug("remoteFilename: \"" + remoteFilename + "\"");
        File remoteFile = new File(remoteFilename);
        logger.debug("remoteFile: \"" + remoteFile.getAbsolutePath() + "\"");
        logger.debug("exists: " + remoteFile.exists());
        if (!remoteFile.exists()) {
            File[] files = remoteFile.getParentFile().listFiles();
            logger.debug("files: " + files.length);
            for (File file : files) {
                logger.debug("file: \"" + file.getAbsolutePath() + "\" - exists: " + file.exists());
            }

            throw new RuntimeException("Remote file does not exist. \"" + remoteFilename + "\"");
        }

        return remoteFile;

    }

}
