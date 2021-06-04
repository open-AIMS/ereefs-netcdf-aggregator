package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.ApplicationContext;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the {@link PipelineFactory} interface to implement the
 * {@link #setSummaryOperator(NcAggregateProductDefinition.SummaryOperator)} method.
 *
 * @author Aaron Smith
 */
abstract public class AbstractOperatorFactory implements PipelineFactory {

    final protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ApplicationContext applicationContext;

    protected NcAggregateProductDefinition.SummaryOperator summaryOperator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setSummaryOperator(NcAggregateProductDefinition.SummaryOperator summaryOperator) {
        this.summaryOperator = summaryOperator;
    }

    @Override
    public String getDescriptor() {
        return this.summaryOperator.getName();
    }
}
