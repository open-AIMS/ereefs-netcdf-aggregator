package aims.ereefs.netcdf.tasks;

import aims.ereefs.netcdf.ApplicationContext;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

/**
 * Public interface for classes that are executed prior to any processing.
 *
 * @author Aaron Smith
 */
public interface PreProcessingTask {

    public boolean supports(NcAggregateProductDefinition.PreProcessingTaskDefn preProcessingTask);

    public void process(NcAggregateProductDefinition.PreProcessingTaskDefn preProcessingTask,
                        ApplicationContext applicationContext);

}