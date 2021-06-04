package aims.ereefs.netcdf.util.file.upload;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of the {@link FileUploadHandler} interface to declare common
 * helpers.
 *
 * @author Aaron Smith
 */
abstract public class AbstractFileUploadHandler implements FileUploadHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Counter apmFileUploadCount = Counter.build()
        .name("ncaggregate_file_upload_count")
        .help("Total number of files uploaded.")
        .labelNames("scheme")
        .register();
    protected static final Gauge apmFileUploadBytes = Gauge.build()
        .name("ncaggregate_file_upload_bytes")
        .help("Total size (bytes) of uploaded files.")
        .labelNames("scheme")
        .register();

}
