package aims.ereefs.netcdf.task.aggregation;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.TaskExecutor;
import aims.ereefs.netcdf.TerminatingException;
import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.aggregator.AggregationPeriodsFactory;
import aims.ereefs.netcdf.input.extraction.ExtractionSite;
import aims.ereefs.netcdf.output.netcdf.OutputDataset;
import aims.ereefs.netcdf.output.netcdf.OutputDatasetBuilder;
import aims.ereefs.netcdf.output.netcdf.SummaryOperatorDefinitionListBuilder;
import aims.ereefs.netcdf.output.summary.OutputWriter;
import aims.ereefs.netcdf.output.summary.SiteBasedCsvFileOutputWriter;
import aims.ereefs.netcdf.output.summary.ZoneBasedCsvFileOutputWriter;
import aims.ereefs.netcdf.task.aggregation.pipeline.PipelineBuilder;
import aims.ereefs.netcdf.task.aggregation.pipeline.TimeInstantIteratorStage;
import aims.ereefs.netcdf.util.file.upload.FileUploadManager;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.task.Task;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * This class executes an {@link NcAggregateTask} for the purposes of aggregating data from one (1)
 * or more NetCDF input files. A {@link NcAggregateTask} generates a single output file, defined in
 * the {@link Task}.
 * <p>
 * The main part of a {@code Task} is the list of {@link NcAggregateTask.TimeInstant}s. A
 * {@code TimeInstant} represents a single time in the output file. For example, if the output
 * file covers a single day, and data is hourly, then the {@code Task} will contain 24
 * {@code TimeInstant} entries. Each {@code TimeInstant} contains a list of
 * {@link NcAggregateTask.Input}s that provide data for that {@code TimeInstant}.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class AggregationTaskExecutor implements TaskExecutor {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Return {@code true} when the {@link Task} is an {@link NcAggregateTask}.
     */
    @Override
    public boolean supports(Task task) {
        return task instanceof NcAggregateTask;
    }

    /**
     * Invoke {@link #supports(Task)} to confirm this class supports the specified {@link Task},
     * and then typecast the task for simpler access.
     */
    @Override
    public void execute(Task task, ApplicationContext applicationContext) {
        if (this.supports(task)) {
            this.execute(
                (NcAggregateTask) task,
                (NcAggregateProductDefinition) applicationContext.getProductDefinition(),
                applicationContext
            );
        }
    }

    /**
     * Execute the specified {@code task}.
     */
    protected void execute(NcAggregateTask task,
                           NcAggregateProductDefinition productDefinition,
                           ApplicationContext applicationContext) {

        // Retrieve references from the ApplicationContext which are used repeatedly within this
        // method, and typecast for convenience.
        final NcAggregateProductDefinition.Action action = productDefinition.getAction();

        // Instantiate any helpers that are used within this method.
        AggregationPeriods aggregationPeriod = AggregationPeriodsFactory.make(
            action.getPeriod()
        );

        // Build the complete list of operations/actions to be performed on the input data based
        // on the defined SummaryOperators in the Product definition, and combine with default MEAN
        // definitions for each declared variable. This becomes the list we work from.
        final List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorDefinitionList =
            SummaryOperatorDefinitionListBuilder.build(
                productDefinition.getAction(),
                applicationContext.getInputDefinitionByVariableNameMap(),
                aggregationPeriod,
                applicationContext.getInputDatasetCache().getReferenceDataset()
            );

        // Create the OutputDataset, which is a wrapper around the actual NetCDF file. The returned
        // object includes an information value object.
        final String outputDatasetLocalFilename = applicationContext.getTempPathname() +
            task.getId() + "-output.nc";
        final String outputDatasetRemoteUrl = task.getBaseUrl() + ".nc";
        OutputDataset outputDataset = null;
        if (productDefinition.getOutputs().getNetcdfOutputFile() != null) {
            outputDataset = OutputDatasetBuilder.build(
                outputDatasetLocalFilename,
                task,
                productDefinition,
                applicationContext,
                aggregationPeriod,
                summaryOperatorDefinitionList,
                applicationContext.getDatasetMetadataIdsByInputIdMap(),
                applicationContext.getMetadataDao()
            );
        }

        // Create the SummaryWriter if defined. This requires a Summary Output File to be defined,
        // and expects that the CSV Static File has been defined with the zoneNamesInputId.
        final String summaryOutputFilename = applicationContext.getTempPathname() + task.getId() +
            "-summary.csv";
        final String summaryOutputRemoteUrl = task.getBaseUrl() + "-summary.csv";
        OutputWriter summaryOutputWriter = null;
        if (productDefinition.getOutputs().getZoneBasedSummaryOutputFile() != null) {
            try {
                summaryOutputWriter = new ZoneBasedCsvFileOutputWriter(
                    new File(summaryOutputFilename),
                    applicationContext,
                    productDefinition.getOutputs().getZoneBasedSummaryOutputFile().getZoneNamesBindName()
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to instantiate the SummaryOutputWriter.", e);
            }
        }
        if (productDefinition.getOutputs().getSiteBasedSummaryOutputFile() != null) {
            try {
                summaryOutputWriter = new SiteBasedCsvFileOutputWriter(
                    new File(summaryOutputFilename),
                    (List<ExtractionSite>) applicationContext.getFromCache(
                        "extractionSites"
                    )
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to instantiate the SummaryOutputWriter.", e);
            }
        }

        try {

            TimeInstantIteratorStage timeInstantIteratorStage =
                PipelineBuilder.build(
                    task,
                    productDefinition,
                    applicationContext,
                    aggregationPeriod,
                    summaryOperatorDefinitionList,
                    outputDataset,
                    summaryOutputWriter
                );
            timeInstantIteratorStage.execute();


            // Complete the write. Note that 'close' on the underlying dataset invokes
            // 'flush'.
            if (outputDataset != null) {
                outputDataset.close();
            }

            // Upload/post the summary file if created.
            if (summaryOutputWriter != null) {
                summaryOutputWriter.flush();
                summaryOutputWriter.close();
                FileUploadManager.upload(
                    summaryOutputFilename,
                    summaryOutputRemoteUrl
                );
            }

            // Upload/post the output dataset.
            if (outputDataset != null) {

                // Generate the Metadata for the output file.
                final String productDefinitionId = productDefinition.getId();
                final String metadataId = task.getMetadataId();
                final String datasetId = metadataId.substring(productDefinitionId.length() + 1);
                final NetCDFMetadataBean netcdfMetadataBean = NetCDFMetadataBean.create(
                    productDefinition.getId(),
                    datasetId,
                    new URI(outputDatasetRemoteUrl),
                    new File(outputDatasetLocalFilename),
                    DateTime.now().getMillis()
                );

                FileUploadManager.upload(
                    outputDatasetLocalFilename,
                    outputDatasetRemoteUrl
                );

                // Persist the metadata to the database if it's valid.
                if (netcdfMetadataBean.getStatus().equals(NetCDFMetadataBean.Status.VALID)) {
                    JSONObject json = netcdfMetadataBean.toJSON();
                    json.put("_id", task.getMetadataId());
                    applicationContext.getMetadataDao().persist(json);
                } else {
                    logger.error("NetCDF metadata reported as NOT valid.");
                    logger.debug(netcdfMetadataBean.toString());
                }

            }

            // Update the Task with the name of the generated files.
            NcAggregateTask refreshedTask = (NcAggregateTask) applicationContext.getTaskDao().getById(task.getId());
            boolean hasUpdatedGeneratedFiles = false;
            if (outputDataset != null) {
                refreshedTask.getOutcome().addGeneratedFile(
                    new NcAggregateTask.GeneratedFile(
                        NcAggregateTask.GeneratedFileType.NETCDF,
                        outputDatasetRemoteUrl
                    )
                );
                hasUpdatedGeneratedFiles = true;
            }
            if (summaryOutputWriter != null) {
                refreshedTask.getOutcome().addGeneratedFile(
                    new NcAggregateTask.GeneratedFile(
                        NcAggregateTask.GeneratedFileType.SUMMARY,
                        summaryOutputRemoteUrl
                    )
                );
                hasUpdatedGeneratedFiles = true;
            }
            if (hasUpdatedGeneratedFiles) {
                applicationContext.getTaskDao().persist(refreshedTask);
            }

        } catch (TerminatingException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("Error thrown.", e);
            throw new RuntimeException("Error thrown.", e);
        }
    }

}
