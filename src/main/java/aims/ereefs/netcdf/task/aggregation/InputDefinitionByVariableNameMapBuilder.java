package aims.ereefs.netcdf.task.aggregation;

import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Variable;

import java.util.*;

/**
 * {@code Builder} to create a look up map that binds a
 * {@link NcAggregateProductDefinition.Input} to each fully qualified variable name.
 *
 * @author Aaron Smith
 */
public class InputDefinitionByVariableNameMapBuilder {

    static protected Logger logger = LoggerFactory.getLogger(InputDefinitionByVariableNameMapBuilder.class);

    static public Map<String, NcAggregateProductDefinition.Input> build(
        NcAggregateProductDefinition productDefinition,
        NcAggregateTask task,
        InputDatasetCache inputDatasetCache,
        Map<String, NcAggregateProductDefinition.Input> inputIdToInputDefinitionMap) {

        final Map<String, NcAggregateProductDefinition.Input> map = new TreeMap<>();

        // Loop through each input source definition.
        for (NcAggregateProductDefinition.Input input : productDefinition.getInputs()) {
            logger.debug("Building map for input \"" + input.getId() + "\".");

            // Ensure the input is for a NetCDF file.
            if (input instanceof NcAggregateProductDefinition.NetCDFInput) {

                // Build a list of available variables for this Product input (as defined in the Product Definition).
                // Only the first input dataset will be used for this Product input, and only for the first TimeInstant.
                // This may need to be expanded in the future, but is currently sufficient for the variables that can be
                // defined in the Product input.
                final List<String> availableVariables = new ArrayList<>();
                final NcAggregateTask.TimeInstant firstTimeInstant = task.getTimeInstants().get(0);
                for (NcAggregateTask.Input timeInstantInput : firstTimeInstant.getInputs()) {
                    if (timeInstantInput.getInputId().equals(input.getId())) {
                        final NcAggregateTask.FileIndexBounds fileIndexBounds = timeInstantInput.getFileIndexBounds().get(0);
                        try (InputDataset inputDataset = inputDatasetCache.retrieve(fileIndexBounds.getMetadataId())) {
                            for (Variable variable : inputDataset.getVariables()) {
                                availableVariables.add(variable.getFullName());
                            }
                        }
                    }
                }

                // Log separately to keep log messages together, otherwise they are interrupted as dataset is
                // downloaded.
                if (availableVariables != null && availableVariables.size() > 0) {
                    logger.debug("Variables available in reference dataset:");
                    Collections.sort(availableVariables);
                    for (String variableName : availableVariables) {
                        logger.debug("- " + variableName);
                    }
                } else {
                    logger.warn("No variables found in reference dataset.");
                }

                // Does the input source definition specify the available variables, or are all variables to be included
                // from the reference dataset?
                String[] variables = ((NcAggregateProductDefinition.NetCDFInput) input).getVariables();

                // If no variable specified in the Product Definition, use all available variables.
                if (variables == null || variables.length == 0) {

                    logger.debug("Product input definition does not identify expected variables. Allowing use of all " +
                            "available variables.");
                    variables = availableVariables.toArray(new String[0]);

                } else {

                    // Variables were specified in the ProductDefinition, so only use variable that are in the reference
                    // dataset.
                    logger.debug("Product input definition identifies expected variables:");
                    List<String> foundVariables = new ArrayList<>();
                    Arrays.sort(variables);
                    for (String variableName : variables) {
                        if (availableVariables.contains(variableName)) {
                            logger.debug("- " + variableName);
                            foundVariables.add(variableName);
                        } else {
                            logger.debug("- " + variableName + " (ignoring)");
                        }
                    }
                    variables = foundVariables.toArray(new String[0]);

                }
                if (variables != null && variables.length > 0) {

                    // Variables have been defined, so loop through each to populate the map.
                    for (String variable : variables) {
                        map.put(input.getId() + "::" + variable, input);
                    }
                }
            }

        }
        return map;
    }
}
