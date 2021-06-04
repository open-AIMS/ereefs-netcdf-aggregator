package aims.ereefs.netcdf.util.file.download;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Abstract base implementation of the {@link FileDownloadHandler} interface to declare common
 * helpers.
 *
 * @author Aaron Smith
 */
abstract public class AbstractFileDownloadHandler implements FileDownloadHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Counter apmFileDownloadCount = Counter.build()
        .name("ncaggregate_file_download_count")
        .help("Total number of files downloaded.")
        .labelNames("scheme")
        .register();
    protected static final Gauge apmFileDownloadBytes = Gauge.build()
        .name("ncaggregate_file_download_bytes")
        .help("Total size (bytes) of downloaded files.")
        .labelNames("scheme")
        .register();
    protected static final Gauge apmFileDownloadDuration = Gauge.build()
        .name("ncaggregate_file_download_duration_seconds")
        .help("The time (seconds) to downloaded a file.")
        .labelNames("scheme")
        .register();

    /**
     * Cached value representing the file download scheme supported by the implementing class. This
     * value is used for instrumentation.
     */
    protected String scheme;

    /**
     * Download the remote file to the local location. {@link FileDownloadHandler} extensions
     * should implement
     */
    @Override
    public File download(String url, String localPath) {
        logger.debug("url: " + url);

        Gauge.Timer durationTimer = apmFileDownloadDuration.labels(this.scheme).startTimer();
        File localFile = this.doDownload(url, localPath);
        durationTimer.setDuration();
        if (localFile != null && localFile.exists()) {
            apmFileDownloadCount.labels(this.scheme).inc();
            apmFileDownloadBytes.labels(this.scheme).inc(localFile.length());
        }
        return localFile;
    }

    /**
     * Abstract template method to implement to perform the actual download.
     */
    abstract protected File doDownload(String url, String localPath);

}
