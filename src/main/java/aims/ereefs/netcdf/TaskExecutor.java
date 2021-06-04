package aims.ereefs.netcdf;

import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;
import au.gov.aims.ereefs.pojo.task.Task;

/**
 * Interface for classes that specialise in performing the actions inherent in a {@link Task}
 * specialisation.
 *
 * @author Aaron Smith
 */
public interface TaskExecutor {

    /**
     * Test method to determine if the specified {@link Task} is supported by the implementing
     * class.
     *
     * @return {@code true} if the implementing class supports the specified {@link Task},
     * {@code false} otherwise.
     */
    public boolean supports(Task task);

    /**
     * Performs the actions inherent in the {@link Task}. This method should only be invoked if
     * {@link #supports(Task)} returned {@code true}.
     *
     * @param task the {@link Task} to process.
     * @param applicationContext the {@link ApplicationContext} that holds application-wide
     *                           references, including the {@link ProductDefinition}
     *                           ({@link ApplicationContext#getProductDefinition()}).
     */
    public void execute(Task task, ApplicationContext applicationContext);

}
