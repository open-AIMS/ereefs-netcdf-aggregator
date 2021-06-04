package aims.ereefs.netcdf.task.aggregation;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.ApplicationContextBuilder;
import aims.ereefs.netcdf.OperationModeExecutor;
import aims.ereefs.netcdf.TaskExecutor;
import au.gov.aims.ereefs.pojo.task.Task;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OperationModeExecutor} implementation for the default operation of the application, namely
 * {@code Aggregation}.
 *
 * @author Aaron Smith
 */
public class AggregationOperationModeExecutor implements OperationModeExecutor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Gauge apmUp = Gauge.build()
        .name("ncaggregate_up")
        .help("Records when/if the instance is up.")
        .labelNames("task_id")
        .register();

    /**
     * Always returns {@code true}.
     */
    @Override
    public boolean supports(String[] args) {
        return true;
    }

    /**
     * Coordinate the {@code Aggregation} operation.
     */
    @Override
    public void execute(String[] args) {

        logger.debug("Executing");

        // Build the ApplicationContext, containing references to properties, helpers and utilities
        // that are static throughout the entire life of the application, but need to be accessed
        // by multiple parts of the application.
        final ApplicationContext applicationContext = ApplicationContextBuilder.build();

        Task task = applicationContext.getTask();

        // Wrap all processing to make sure the DB connection is closed, and that the metrics
        // monitoring registers up and down.
        try {

            // Register the start of the app with the metrics monitoring system.
            apmUp.labels(task.getId()).set(1);

            // Offer the Task to all registered TaskExecutor implementations.
            TaskExecutor aggregationTaskExecutor = new AggregationTaskExecutor();
            if (aggregationTaskExecutor.supports(task)) {
                aggregationTaskExecutor.execute(task, applicationContext);
            }

            logger.info("Processing successful.");

        } finally {

            // Register the end of the app with the metrics monitoring system.
            apmUp.labels(task.getId()).set(0);

            if (applicationContext.getMongoClient() != null) {
                applicationContext.getMongoClient().close();
            }
        }

    }

}