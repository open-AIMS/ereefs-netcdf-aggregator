package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

/**
 * Interface for {@code Factory} classes that instantiate and initialise a {@link Pipeline} for
 * data processing.
 *
 * @author Aaron Smith
 */
public interface PipelineFactory {

    /**
     * Test to determine if the implementing {@code Factory} supports instantiation of the specified
     * {@link NcAggregateProductDefinition.SummaryOperator#getOperatorType()}.
     *
     * @param operatorType the type of operator to be instantiated.
     * @return {@code true} if the operator is supported, {@code false} otherwise.
     */
    boolean supports(String operatorType);

    /**
     * Cache of the global {@link ApplicationContext}.
     */
    void setApplicationContext(ApplicationContext applicationContext);

    /**
     * Cache the specified {@link NcAggregateProductDefinition.SummaryOperator}.
     */
    void setSummaryOperator(NcAggregateProductDefinition.SummaryOperator summaryOperator);

    /**
     * Factory method for instantiating a {@link Pipeline} implementation class based on
     * the configuration object and registry previously cached.
     *
     * @see #setSummaryOperator(NcAggregateProductDefinition.SummaryOperator)
     */
    Pipeline make();

    /**
     * Description for logging purposes.
     */
    String getDescriptor();

}
