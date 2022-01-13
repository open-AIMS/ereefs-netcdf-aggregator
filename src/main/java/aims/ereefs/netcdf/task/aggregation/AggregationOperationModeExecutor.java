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
 * {@link OperationModeExecutor} implementation for the default aggregation operation of the application.
 *
 * @author Aaron Smith
 */
public class AggregationOperationModeExecutor implements OperationModeExecutor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Define the metric to be reported to Prometheus.
     */
    protected static final Gauge apmUp = Gauge.build()
            .name("ncaggregate_up")
            .help("Records when/if the instance is up.")
            .labelNames("task_id")
            .register();

    /**
     * Always returns {@code true}. If a previous{@link OperationModeExecutor} does not handle the
     * request first, this implementation will.
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

        // Build the ApplicationContext, populated with references to properties, helpers and
        // utilities that are immutable throughout the life of the application. Note that this
        // process pulls some parameters from the execution environment.
        final ApplicationContext applicationContext = ApplicationContextBuilder.build();

        // Get the Task that was loaded from the database during the build of the
        // ApplicationContext. The Task instructs precisely what needs to be done.
        Task task = applicationContext.getTask();

        // Wrap all processing to make sure the DB connection is closed, and that the metrics
        // monitoring registers "start" and "finish".
        try {

            // Register the start of execution with the metrics monitoring system.
            apmUp.labels(task.getId()).set(1);

            // Offer the Task to all registered TaskExecutor implementations. This is a level of
            // abstraction that is currently not necessary as there is only one (1) TaskExecutor
            // implementation.
            TaskExecutor aggregationTaskExecutor = new AggregationTaskExecutor();
            if (aggregationTaskExecutor.supports(task)) {
                aggregationTaskExecutor.execute(task, applicationContext);
            }

            logger.info("Processing successful.");

        } finally {

            // Register the end of execution with the metrics monitoring system.
            apmUp.labels(task.getId()).set(0);

            // Close the connection to the database. This is handled inconsistently across the
            // application and should be refactored.
            if (applicationContext.getMongoClient() != null) {
                applicationContext.getMongoClient().close();
            }
        }

    }

}
