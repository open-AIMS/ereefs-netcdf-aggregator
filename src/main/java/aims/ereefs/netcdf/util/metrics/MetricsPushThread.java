package aims.ereefs.netcdf.util.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.URL;
import java.util.Map;

/**
 * Thread for pushing metrics to the Push Gateway.
 *
 * @author Aaron Smith
 */
public class MetricsPushThread extends Thread {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected PushGateway pushGateway = null;
    protected CollectorRegistry registry = null;
    protected String appName;
    protected int intervalSeconds = 15;
    protected Map<String, String> groupingKey;

    /**
     * Cache of the last exception that occurred when attempting to Push. Subsequent error messages are not logged.
     */
    protected Throwable lastException = null;

    public MetricsPushThread(URL connectionUrl,
                             CollectorRegistry registry,
                             String appName,
                             Map<String, String> groupingKey,
                             int intervalSeconds) {
        logger.debug("connectionUrl: " + connectionUrl);
        logger.debug("appName: " + appName);
        this.pushGateway = new PushGateway(connectionUrl);
        this.registry = registry;
        this.appName = appName;
        this.intervalSeconds = intervalSeconds;
        this.groupingKey = groupingKey;
    }

    public void run() {
        boolean isInterrupted = false;
        while (!isInterrupted) {
            try {
                try {
                    Thread.sleep(this.intervalSeconds * 1000);
                } catch (InterruptedException ignore) {
                    isInterrupted = true;
                }
                logger.debug("Pushing metrics.");
                this.pushGateway.pushAdd(
                    this.registry,
                    this.appName,
                    this.groupingKey
                );
                logger.debug("Push complete.");
            } catch (Exception e) {
                if (this.lastException == null || !this.lastException.getClass().equals(e.getClass())) {
                    logger.error("Failed to push to PushGateway.", e);
                } else {
                    logger.error("Failed to push to PushGateway: \"" + e.getClass().getName() + ": " + e.getMessage() + "\"");
                }
                this.lastException = e;
            }
        }
        logger.debug("Push thread terminating.");
    }

}