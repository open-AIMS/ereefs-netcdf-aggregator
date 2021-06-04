package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A {@link NcAggregateProductDefinition.SummaryOperator} identifies a single action to perform as
 * part of the processing for the application. Depending on the action, the {@code SummaryOperator}
 * will have one or more input variables. {@code SummaryOperators} can be specifically defined
 * (normal for complex operations such as speed calculations, and threshold processing), or simply
 * referenced for {@code Mean} calculations (or {@code no} processing in special cases). This class
 * combines these two (2) definition options into a complete list of actions to be performed by the
 * application. Variables must
 * be prefixed by their Input Source Id, such as "inputSourceId"/"variableName".
 *
 * @author Aaron Smith
 */
public class SummaryOperatorDefinitionListBuilder {

    static protected Logger logger = LoggerFactory.getLogger(SummaryOperatorDefinitionListBuilder.class);

    /**
     * Build a complete list of {@link NcAggregateProductDefinition.SummaryOperator}s representing
     * the complete processing to be performed by the application.
     *
     * @param actionDefinition                 the {@code Action} section of the
     *                                         {@code ProductDefinition}.
     * @param inputDefinitionByVariableNameMap a map whose keyset is a list of all variables
     *                                         supported by the application. Note that the
     *                                         variable names are prefixed with the Input Source
     *                                         Id.
     * @param aggregationPeriod                the period over which aggregation is performed. This
     *                                         is used to determine if {@code MEAN} or {@code NONE}
     *                                         is used.
     */
    static public List<NcAggregateProductDefinition.SummaryOperator> build(
        NcAggregateProductDefinition.Action actionDefinition,
        Map<String, NcAggregateProductDefinition.Input> inputDefinitionByVariableNameMap,
        AggregationPeriods aggregationPeriod,
        InputDataset referenceDataset
    ) {

        // Declare the list to be built.
        final List<NcAggregateProductDefinition.SummaryOperator> summaryOperatorList = new ArrayList<>();

        // Process any SummaryOperator definitions first.
        final NcAggregateProductDefinition.SummaryOperator[] summaryOperators = actionDefinition.getSummaryOperators();
        if (summaryOperators != null) {
            for (NcAggregateProductDefinition.SummaryOperator summaryOperator : summaryOperators) {
                if (summaryOperator.isEnabled()) {

                    // Prefix each variable with InputSourceId if missing.
                    List<String> fullQualifiedVariableNames = new ArrayList<>();
                    for (String variableName : summaryOperator.getInputVariables()) {
                        if (variableName.contains("::")) {
                            fullQualifiedVariableNames.add(variableName);
                        } else {
                            // Find the first input source that offers this variable.
                            String prefixedVariable = findFullyQualifiedVariableName(
                                variableName,
                                inputDefinitionByVariableNameMap.keySet()
                            );
                            if (prefixedVariable != null) {
                                fullQualifiedVariableNames.add(prefixedVariable);
                            }
                        }
                    }

                    // If all input variables were found, replace the existing list of input variables with a list of
                    // fully qualified variable names.
                    if (summaryOperator.getInputVariables().size() == fullQualifiedVariableNames.size()) {
                        summaryOperator.getInputVariables().clear();
                        summaryOperator.getInputVariables().addAll(fullQualifiedVariableNames);

                        logger.debug(summaryOperator.toString());
                        summaryOperatorList.add(summaryOperator);
                    } else {
                        logger.warn("Input variables missing for \"" + summaryOperator.toString() + "\". Ignoring.");
                    }
                }
            }
        }

        // Handle variables specified in the Action definition. If only a single variable "_all_" is
        // specified, load a list of all variables that have at least 3 dimensions.
        String[] variableNames = actionDefinition.getVariables();
        if (referenceDataset != null && variableNames != null && variableNames.length == 1 &&
            variableNames[0].equalsIgnoreCase("_all_")) {
            variableNames =
                referenceDataset
                    .getVariables()
                    .stream()
                    .filter(variable -> variable.getDimensions().size() >= 3)
                    .map(variable -> variable.getShortName())
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
        }
        if (variableNames != null) {
            for (String variableName : variableNames) {

                // Build a name that includes the inputId.
                String fullyQualifiedVariableName = findFullyQualifiedVariableName(
                    variableName,
                    inputDefinitionByVariableNameMap.keySet()
                );

                // Only add the variable if the fully qualified name could be built.
                if (fullyQualifiedVariableName != null) {
                    final NcAggregateProductDefinition.SummaryOperator summaryOperator =
                        new NcAggregateProductDefinition.SummaryOperator();
                    if (aggregationPeriod.equals(AggregationPeriods.NONE)) {
                        summaryOperator.setOperatorType("none");
                        summaryOperator.setName(fullyQualifiedVariableName);
                    } else {
                        summaryOperator.setOperatorType("MEAN");
                        summaryOperator.setName("Mean of " + fullyQualifiedVariableName);
                    }
                    summaryOperator.getInputVariables().add(fullyQualifiedVariableName);
                    NcAggregateProductDefinition.OutputVariable outputVariable =
                        new NcAggregateProductDefinition.OutputVariable();
                    outputVariable.getAttributes().put("short_name", variableName);

                    // Add attributes if we have a reference dataset.
                    if (referenceDataset != null) {
                        final Variable variable = referenceDataset.findVariable(variableName);
                        // Ignore if the variable doesn't exist in the reference dataset.
                        if (variable != null) {
                            for (Attribute attribute : variable.getAttributes()) {
                                if (!attribute.getShortName().startsWith("_")) {
                                    outputVariable.getAttributes().put(
                                        attribute.getShortName(),
                                        attribute.getStringValue()
                                    );
                                }
                            }
                        }
                    }

                    summaryOperator.getOutputVariables().add(outputVariable);
                    logger.debug(summaryOperator.toString());
                    summaryOperatorList.add(summaryOperator);
                }
            }
        }

        if (summaryOperatorList.isEmpty()) {
            logger.warn("No summary operators configured.");
        }

        return summaryOperatorList;
    }

    /**
     * Helper method to find the first input source/variable name combination that contains a
     * variable name that matches.
     */
    static protected String findFullyQualifiedVariableName(String variableName,
                                                           Set<String> prefixedVariableNames) {
        for (String prefixedVariableName : prefixedVariableNames) {
            if (prefixedVariableName.endsWith("::" + variableName)) {
                return prefixedVariableName;
            }
        }
        return null;
    }
}
