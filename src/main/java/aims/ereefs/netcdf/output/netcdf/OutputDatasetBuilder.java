package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * Helper class responsible for creating a {@code NetCdf} file for the output dataset, and
 * populating the file with the basic information required (eg: dimensions, global attributes, etc).
 *
 * @author Aaron Smith
 */
public class OutputDatasetBuilder {

    static protected Logger logger = LoggerFactory.getLogger(OutputDatasetBuilder.class);

    /**
     * Create the necessary output file, returning a {@link OutputDataset} object that wraps
     * the methods needed to interact with the output file.
     */
    static public OutputDataset build(String filename,
                                      NcAggregateTask task,
                                      NcAggregateProductDefinition productDefinition,
                                      ApplicationContext applicationContext,
                                      AggregationPeriods aggregationPeriods,
                                      List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorDefinitionList,
                                      Map<String, List<String>> datasetMetadataIdsByInputIdMap,
                                      MetadataDao metadataDao) {


        // Grab a reference to the temporary "local" file.
        logger.debug("filename: " + filename);
        final File file = new File(filename);

        // Create the Writer which is used to create and populate the file.
        NetcdfFileWriter writer = null;
        try {
            writer = NetcdfFileWriter.createNew(
                NetcdfFileWriter.Version.netcdf4,
                filename
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a NetcdfFileWriter.", e);
        }

        // Create the Root Group because that is what it needs...
        Group rootGroup = writer.addGroup(null, "rootGroup");

        // Wrap the writer in an OutputDataset to provide wrappers for populating the dataset.
        OutputDataset outputDataset = new OutputDataset(writer, rootGroup);

        // Obtain a reference dataset that will be used when information is required from a
        // representative input dataset.
        final InputDataset referenceDataset =
            applicationContext.getInputDatasetCache().getReferenceDataset();

        // Obtain a RegularGridMapper if required.
        RegularGridMapper regularGridMapper = null;
        final String bindName =
            productDefinition.getOutputs().getNetcdfOutputFile().getRegularGridMapperCacheBindName();
        if (bindName != null) {
            regularGridMapper = (RegularGridMapper) applicationContext.getFromCache(bindName);
        }

        // During initialisation, the dataset can be modified for dimensions and variable
        // definitions. This is supposed to be done before the dataset is written to disk,
        // which occurs when the OutputDatasetFactory invokes writer.create() and
        // write.setRedefineMode().
        try {

            // IMPORTANT !!!
            // ncAggregate supports multiple input sources being combined into a single
            // output file. This means that dimensions and variable structures must be
            // combined from each input source. Where a variable structure or dimension
            // is defined in multiple input sources, the first input source is used.

            // Populate the global attributes. These are taken from reference datasets which
            // are the all referenced in the first TimeInstant for the Task. Note that
            // global attributes can also be defined in the ProductDefinition.
            GlobalAttributesPopulator.populate(
                outputDataset,
                task,
                productDefinition,
                datasetMetadataIdsByInputIdMap,
                metadataDao,
                aggregationPeriods
            );

            // Populate the dimensions using the first input dataset from the first
            // TimeInstant from the Task.
            final Map<String, Dimension> inputDimensionNameToOutputDimensionMap =
                DimensionsPopulator.populate(
                    outputDataset,
                    referenceDataset,
                    regularGridMapper
                );

            // Copy the structure of the variables.
            VariableStructurePopulator.populate(
                outputDataset,
                task,
                productDefinition,
                applicationContext.getInputDatasetCache(),
                summaryOperatorDefinitionList,
                inputDimensionNameToOutputDimensionMap,
                aggregationPeriods
            );

            // Copy the structure of the variables that contain dimensional data. eg: time, depth, etc.
            DimensionVariablesStructurePopulator.populate(
                outputDataset,
                referenceDataset,
                inputDimensionNameToOutputDimensionMap,
                regularGridMapper
            );

            logger.debug("Creating output file.");
            writer.create();
            logger.debug("Output file created.");
            writer.setRedefineMode(false);

            // Copy the data of the variables that contain dimensional data.
            DimensionsVariableDataPopulator.populate(
                task,
                referenceDataset,
                outputDataset,
                regularGridMapper);

        } catch (Exception e) {
            throw new RuntimeException("Failed to copy dimension variable data.", e);
        }

        // Close the input dataset to free memory.
        referenceDataset.close();

        return outputDataset;

    }

}
