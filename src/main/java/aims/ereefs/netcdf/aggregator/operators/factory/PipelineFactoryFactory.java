package aims.ereefs.netcdf.aggregator.operators.factory;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.ThresholdExceedancePipelineFactory;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@code Pool} of {@link PipelineFactory} implementations for instantiating a
 * {@link aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline} to perform the processing
 * defined by the {@code Product} definition.
 *
 * @author Aaron Smith
 */
public class PipelineFactoryFactory {

    static final protected Logger logger = LoggerFactory.getLogger(PipelineFactoryFactory.class);

    /**
     * A list of all supported {@link PipelineFactory} implementations.
     */
    static protected List<PipelineFactory> supportedFactories = new ArrayList<PipelineFactory>() {{
        add(new NoneOperatorFactory());
        add(new MaxOperatorFactory());
        add(new MeanOperatorFactory());
        add(new MinOperatorFactory());
        add(new RangeOperatorFactory());
        add(new SpeedOperatorFactory());
        add(new SpeedMeanOperatorFactory());
        add(new SumOperatorFactory());
        add(new ThresholdExceedancePipelineFactory());
        add(new DifferenceOperatorFactory());
    }};

    /**
     * Instantiate a {@link PipelineFactory} for the corresponding configuration object.
     */
    static public PipelineFactory make(NcAggregateProductDefinition.SummaryOperator summaryOperator,
                                       ApplicationContext applicationContext) {

        // Determine and log the unique operator type.
        final String operatorType = summaryOperator.getOperatorType();
        final String operatorName = (
            summaryOperator.getName() != null ?
                summaryOperator.getName() :
                "Name not specified"
        );
        if (logger.isTraceEnabled()) {
            logger.trace("operator: " + operatorName + " (" + operatorType + ")");
        }

        // Find the PipelineFactory that supports this operator type.
        final Optional<PipelineFactory> optional = supportedFactories.stream()
            .filter(factory -> factory.supports(operatorType))
            .findFirst();
        if (!optional.isPresent()) {
            throw new RuntimeException("Operator \"" + operatorName + " (" + operatorType + ")" +
                "\" not supported.");
        }
        PipelineFactory pipelineFactory = optional.get();
        logger.debug("Using " + pipelineFactory.getClass().getName());

        //
        pipelineFactory.setApplicationContext(applicationContext);
        pipelineFactory.setSummaryOperator(summaryOperator);
        return pipelineFactory;

    }

}
