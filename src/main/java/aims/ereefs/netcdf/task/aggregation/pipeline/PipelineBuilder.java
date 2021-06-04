package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.output.netcdf.OutputDataset;
import aims.ereefs.netcdf.output.summary.OutputWriter;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * {@code Builder} class for instantiating and initialising the {@code Pipeline}, returning a
 * reference to the first {@code Stage} for execution.
 *
 * @author Aaron Smith
 */
public class PipelineBuilder {

    public Logger logger = LoggerFactory.getLogger(PipelineBuilder.class);

    final static public TimeInstantIteratorStage build(NcAggregateTask task,
                                                       NcAggregateProductDefinition productDefinition,
                                                       ApplicationContext applicationContext,
                                                       AggregationPeriods aggregationPeriod,
                                                       List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorDefinitionList,
                                                       OutputDataset outputDataset,
                                                       OutputWriter summaryOutputWriter) {

        // Instantiate the context for the Pipeline for communication between the Stages.
        final PipelineContext pipelineContext = new PipelineContext(
            task,
            productDefinition,
            outputDataset != null,
            summaryOutputWriter
        );

        // Time Instant Iterator - iterate through each TimeInstant.
        final TimeInstantIteratorStage timeInstantIteratorStage = new TimeInstantIteratorStage();
        timeInstantIteratorStage.setPipelineContext(pipelineContext);

        // Time Instant Executor - executing a single TimeInstant.
        final TimeInstantExecutorStage timeInstantExecutorStage = new TimeInstantExecutorStage();
        timeInstantExecutorStage.setPipelineContext(pipelineContext);
        timeInstantIteratorStage.setNextStage(timeInstantExecutorStage);

        // Operator Iterator - iterate through each Operator.
        final OperatorIteratorStage operatorIteratorStage = new OperatorIteratorStage(
            summaryOperatorDefinitionList
        );
        operatorIteratorStage.setPipelineContext(pipelineContext);
        timeInstantExecutorStage.setNextStage(operatorIteratorStage);

        // Input - executing a single Input.
        final InputIteratorStage inputIteratorStage = new InputIteratorStage();
        inputIteratorStage.setPipelineContext(pipelineContext);
        operatorIteratorStage.setNextStage(inputIteratorStage);

        // Operator Executor - execute a single Operator.
        final OperatorExecutorStage operatorExecutorStage = new OperatorExecutorStage();
        operatorExecutorStage.setPipelineContext(pipelineContext);
        inputIteratorStage.setNextStage(operatorExecutorStage);

        // Write Time Slice.
        final WriteTimeSliceStage writeTimeSliceStage = new WriteTimeSliceToOutputDatasetStage(
            pipelineContext,
            outputDataset
        );

        // Obtain a reference dataset that will be used when information is required from a
        // representative input dataset.
        final InputDataset referenceDataset =
            applicationContext.getInputDatasetCache().getReferenceDataset();

        // If a NetCDF output file has been specified, has a RegularGridMapper also been specified?
        RegularGridMapper regularGridMapper = null;
        final NcAggregateProductDefinition.NetcdfOutputFile netcdfOutputFile =
            productDefinition.getOutputs().getNetcdfOutputFile();
        if (netcdfOutputFile != null) {
            final String bindName = netcdfOutputFile.getRegularGridMapperCacheBindName();
            if (bindName != null) {
                regularGridMapper = (RegularGridMapper) applicationContext.getFromCache(bindName);
            }
        }
        final RegularGriddingStage regularGriddingStage = new RegularGriddingStage(
            pipelineContext,
            regularGridMapper,
            writeTimeSliceStage
        );
        referenceDataset.close();

        // Accumulation.
        final AccumulationStage accumulationStage = new AccumulationStage(
            applicationContext,
            pipelineContext,
            applicationContext.getInputDatasetCache(),
            aggregationPeriod,
            regularGriddingStage
        );
        operatorExecutorStage.setAccumulationStage(accumulationStage);

        return timeInstantIteratorStage;

    }
}
