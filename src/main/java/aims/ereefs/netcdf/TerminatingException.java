package aims.ereefs.netcdf;

import au.gov.aims.ereefs.pojo.task.Task;

/**
 * Custom exception that extends {@code RuntimeException}, thrown when the application has detected
 * that it is being terminated by an external force via the {@link Task#getStatus()} property.
 */
public class TerminatingException extends RuntimeException {
    public TerminatingException(String msg) {
        super(msg);
    }
}
