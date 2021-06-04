package aims.ereefs.netcdf.util.file.upload;

/**
 * Interface for {@code Handler} classes that provide specialised support for uploading files.
 *
 * @author Aaron Smith
 */
public interface FileUploadHandler {

    /**
     * Checks if the specified {@code url} is supported by the {@link FileUploadHandler}
     * implementation.
     *
     * @param url the URL including the prefixed storage mechanism.
     * @return {@code true} if the {@code url} is supported, {@code false} otherwise.
     */
    boolean supports(String url);

    /**
     * Upload the file based on the supported storage mechanism. This method should only be
     * invoked if {@link #supports(String)} returned {@code true}.
     *
     * @param localFilename the local location of the file to publish.
     * @param url           the remote location of the file to publish. This value includes a prefix that
     *                      identifies the storage mechanism.
     */
    void upload(String localFilename, String url);

}