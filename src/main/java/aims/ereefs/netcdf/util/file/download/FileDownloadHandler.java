package aims.ereefs.netcdf.util.file.download;

import java.io.File;

/**
 * Interface for {@code Handler} classes that provide specialised support for downloading files.
 *
 * @author Aaron Smith
 */
public interface FileDownloadHandler {

    /**
     * Checks if the specified {@code url} is supported by the {@link FileDownloadHandler}
     * implementation.
     *
     * @param url the URL including the prefixed storage mechanism.
     * @return {@code true} if the {@code url} is supported, {@code false} otherwise.
     */
    boolean supports(String url);

    /**
     * Retrieve the size of the file.
     *
     * @param url the location of the file. This value includes a prefix that identifies the
     *            storage mechanism.
     */
    long getFileSize(String url);

    /**
     * Download the file based on the supported storage mechanism. This method should only be
     * invoked if {@link #supports(String)} returned {@code true}.
     *
     * @param url       the location of the file to download. This value includes a prefix that
     *                  identifies the storage mechanism.
     * @param localPath the local location to store the file after downloading.
     * @return reference to the local {@code File}. This value will be {@code null} if the remote
     * file does not exist.
     */
    File download(String url, String localPath);

    /**
     * Delete the {@code downloaded} file based on the supported storage mechanism. This allows the
     * {@link FileDownloadHandler} to determine if deleting a file is permitted, which it might not
     * be if the file was local to start with.
     */
    boolean delete(File downloadedFile);
}