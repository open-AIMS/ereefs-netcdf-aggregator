package aims.ereefs.netcdf.util.metrics;

import aims.ereefs.netcdf.util.EnvironmentVariableReader;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;

/**
 * {@code Factory} to instantiate a {@link MetricsPushThread} instance.
 *
 * @author Aaron Smith
 */
public class MetricsPushThreadFactory {

    static protected Logger logger = LoggerFactory.getLogger(MetricsPushThreadFactory.class);

    static public MetricsPushThread make() {

        // Identify the URL for the PushGateway.
        String pushGatewayConnectionUrl =
            EnvironmentVariableReader.getInstance().optByKey("PROMETHEUS_PUSH_GATEWAY_URL");

        // Check and action if the URL is actually a pointer to another variable (ie: starts with $).
        if (pushGatewayConnectionUrl != null && pushGatewayConnectionUrl.startsWith("$")) {
            pushGatewayConnectionUrl = "http://" +
                EnvironmentVariableReader.getInstance().optByKey(pushGatewayConnectionUrl.substring(1)) + ":9091";
        }

        // If the URL was not specified, do not instantiate a MetricsPushThread.
        if (pushGatewayConnectionUrl != null) {

            // Enable default exports.
            logger.debug("Enabling default exports.");
            DefaultExports.initialize();

            try {

                logger.debug("Starting push thread.");
                MetricsPushThread metricsPushThread = new MetricsPushThread(
                    new URL(pushGatewayConnectionUrl),
                    CollectorRegistry.defaultRegistry,
                    "ereefs-ncaggregate",
                    new HashMap<String, String>() {{
                        put("instance", InetAddress.getLocalHost().getHostName());
                        put("task_id", EnvironmentVariableReader.getInstance().getByKey("TASK_ID"));
                    }},
                    15
                );
                metricsPushThread.start();
                return metricsPushThread;
            } catch (Exception e) {
                logger.error("Failed to instantiate a MetricsPushThread.", e);
            }
        }
        return null;
    }

}