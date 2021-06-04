package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import aims.ereefs.netcdf.util.Constants;
import aims.ereefs.netcdf.util.dataset.DatasetUtils;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility class for populating the Netcdf Output file with the variable structure from the
 * reference Netcdf Input file. This class also populates
 * {@link OutputDatasetInfo#summaryOperatorVariablesList}.
 *
 * @author Aaron Smith
 */
public class VariableStructurePopulator {

    static protected Logger logger = LoggerFactory.getLogger(VariableStructurePopulator.class);

    static public void populate(OutputDataset outputDataset,
                                NcAggregateTask task,
                                NcAggregateProductDefinition productDefinition,
                                InputDatasetCache inputDatasetCache,
                                List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorDefinitionList,
                                Map<String, Dimension> inputDimensionNameToOutputDimensionMap,
                                AggregationPeriods aggregationPeriods
    ) throws IOException, InvalidRangeException {

        logger.debug("----- Start -----");

        // Grab references for easier use.
        OutputDatasetInfo outputDatasetInfo = outputDataset.getOutputDatasetInfo();
        List<SummaryOperatorVariables> summaryOperatorVariablesList =
            outputDatasetInfo.summaryOperatorVariablesList;

        // Process each configured operator.
        summaryOperatorDefinitionList.stream().forEach(summaryOperatorDefn -> {

            logger.debug("summaryOperatorDefn: " + summaryOperatorDefn);

            // Declare the lists that will be populated with input and output variable for this
            // summary object.
            final List<Variable> inputVariables = new ArrayList<>();
            final List<Variable> outputVariables = new ArrayList<>();

            // Build a list of actual input variables, retrieved from the corresponding dataset(s).
            for (final String fullyQualifiedName : summaryOperatorDefn.getInputVariables()) {
                final String[] variableNameTokens = fullyQualifiedName.split(
                    Constants.VARIABLE_NAME_SEPARATOR
                );
                if (variableNameTokens.length != 2) {
                    throw new RuntimeException("Variable \"" + fullyQualifiedName +
                        "\" not fully qualified.");
                }
                final String inputId = variableNameTokens[0];
                final String shortInputVariableName = variableNameTokens[1];

                // Retrieve a reference dataset for the input variable.
                final InputDataset referenceDataset = inputDatasetCache.getReferenceDataset(inputId);

                // Retrieve and cache the variable from the input dataset.
                final Variable inputVariable = referenceDataset.findVariable(shortInputVariableName);
                if (inputVariable == null) {
                    throw new RuntimeException("Variable \"" + fullyQualifiedName +
                        "\" not found in dataset (inputId: \"" + inputId + "\").");
                }
                inputVariables.add(inputVariable);

                // Close the reference dataset to free memory.
                referenceDataset.close();
            }

            // Determine if any of the input variables have a time dimension. If so, add an
            // attribute to the output variable definitions stating the aggregation period.
            boolean hasTimeDimension = inputVariables.stream()
                .anyMatch(inputVariable -> {
                    return inputVariable.getDimensions().stream()
                        .anyMatch(dimension -> {
                            return dimension.getFullName().equals("time");
                        });
                });
            if (hasTimeDimension) {
                summaryOperatorDefn.getOutputVariables()
                    .forEach(outputVariableDefn -> {
                        outputVariableDefn.getAttributes().putIfAbsent(
                            "aggregation",
                            aggregationPeriods.description
                        );
                    });
            }

            // For each output variable defined, create an output variable in the output dataset
            // using the dimensions of the first input variable.
            Variable firstInputVariable = inputVariables.get(0);
            DataType dataType = firstInputVariable.getDataType();
            List<Dimension> dimensionsForOutputVar =
                DatasetUtils.findOutputDimensionsByInputVariableName(
                    firstInputVariable,
                    inputDimensionNameToOutputDimensionMap
                );

            summaryOperatorDefn.getOutputVariables()
                .forEach(outputVariableDefn -> {
                    String shortName = outputVariableDefn.getAttributes().get("short_name");
                    Variable outputVariable = outputDataset.addVariable(
                        shortName,
                        dataType,
                        dimensionsForOutputVar
                    );

                    DatasetUtils.copyVariableAttributes(
                        firstInputVariable,
                        outputVariable,
                        outputVariableDefn.getAttributes()
                    );
                    outputVariables.add(outputVariable);
                    outputDatasetInfo.outputVariableMap.put(shortName, outputVariable);
                });

            // Capture the relationship for later use.
            SummaryOperatorVariables details = new SummaryOperatorVariables();
            details.summaryOperatorConfig = summaryOperatorDefn;
            details.inputVariables = inputVariables;
            details.outputVariables = outputVariables;
            summaryOperatorVariablesList.add(details);

        });

        // Write a warning to the logs if SummaryOperatorVariablesList is empty.
        if (summaryOperatorVariablesList.isEmpty()) {
            logger.warn("WARNING: No summary operators defined!");
        }

        logger.debug("----- End -----");
    }

}
