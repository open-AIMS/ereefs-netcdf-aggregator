package aims.ereefs.netcdf;

import aims.ereefs.netcdf.metadata.populate.PopulateMetadataOperationModeExecutor;
import aims.ereefs.netcdf.regrid.RegridOperationModeExecutor;
import aims.ereefs.netcdf.task.aggregation.AggregationOperationModeExecutor;
import aims.ereefs.netcdf.util.metrics.MetricsPushThread;
import aims.ereefs.netcdf.util.metrics.MetricsPushThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimeZone;

/**
 * Main class invoked when the tool is launched.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    static public void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Brisbane"));

        // Log any environmental conditions that might be useful for debugging purposes.
        logger.info("Start: " + new Date());
        logger.info("Location: " + Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (logger.isDebugEnabled()) {
            EnvironmentLogger.dump(args);
        }

        // Wrap all processing in try..catch so the app exits with a non-zero value on error.
        try {

            // Wrap all processing for metrics monitoring, so that all metrics are pushed before
            // exit.
            MetricsPushThread metricsPushThread = MetricsPushThreadFactory.make();
            try {

                // Handle the operation modes supported by the application. Note that the
                // AggregationOperationModeExecutor is added last as it is the default handler.
                final OperationModeExecutor[] executors = new OperationModeExecutor[]{
                    new PopulateMetadataOperationModeExecutor(),
                    new RegridOperationModeExecutor(),
                    new AggregationOperationModeExecutor()
                };
                boolean isHandled = false;
                for (final OperationModeExecutor executor : executors) {
                    if (!isHandled) {
                        if (executor.supports(args)) {
                            executor.execute(args);
                            isHandled = true;
                        }
                    }
                }
            } finally {
                logger.info("Application exiting.");

                // If metrics are being pushed to a PushGateway, interrupt the thread if it is
                // sleeping and wait for it to do a final push before continuing to the exit.
                if (metricsPushThread != null) {
                    logger.debug("Halting push thread.");
                    metricsPushThread.interrupt();
                    metricsPushThread.join();
                }

            }

        } catch (TerminatingException e) {
            logger.error("Processing terminated.");
            System.exit(1);
        } catch (Exception e) {
            logger.error("Processing failed.", e);
            System.exit(1);
        }
        logger.debug("Application exited normally.");
    }

}