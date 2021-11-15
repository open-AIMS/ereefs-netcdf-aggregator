package aims.ereefs.netcdf.util.dataset;

import aims.ereefs.netcdf.output.netcdf.OutputDataset;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.unidata.util.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Utilities for accessing and/or processing Netcdf Datasets.
 * <p>
 * The following steps are required to create a new NetCDF file:
 *
 * <ul>
 *     <li>Copy Global Attributes.</li>
 *     <li>Copy the dimensions. Note that dimensions themselves do not contain any data. The data is
 *     actually contained in the <code>DimensionVariables</code>.</li>
 *     <li>Copy the variables structure.</li>
 *     <li>Copy the Dimension Variables structure.</li>
 *     <li>Copy the Dimension Variables data.</li>
 * </ul>
 *
 * @author Aaron Smith
 */
public class DatasetUtils {

    static protected Logger logger = LoggerFactory.getLogger(DatasetUtils.class);

    /**
     * Convenience method to find the {@code time} variable. This method invoked
     * {@link #findVariable(NetcdfDataset, String)}.
     */
    static public Variable findTimeVariable(NetcdfDataset dataset) {
        return findVariable(dataset, "time");
    }

    /**
     * Find the variable identified by {@code shortName} within the specified {@code dataset}. This
     * method returns {@code null} if a variable matching the {@code shortName} cannot be found.
     * <p>
     * This method is required because the <code>NetcdfDataSet.findVariable()</code> method seems
     * to be buggy.
     */
    static public Variable findVariable(NetcdfDataset dataset, String shortName) {
        for (Variable variable : dataset.getVariables()) {
            if (variable.getShortName().equals(shortName)) {
                return variable;
            }
        }
        return null;
    }

    /**
     * for a given input variable tell me the dimensions of the corresponding output variable
     */
    static public List<Dimension> findOutputDimensionsByInputVariableName(
        Variable inputVariable,
        Map<String, Dimension> outputDimensions) {
        List<Dimension> dimensionsForOutputVar = new ArrayList<Dimension>();
        final List<Dimension> dimensionsForInputVar = inputVariable.getDimensions();
        for (Dimension dimension : dimensionsForInputVar) {
            final Dimension outputDimension = outputDimensions.get(dimension.getFullName());
            if (outputDimension != null) {
                dimensionsForOutputVar.add(outputDimension);
            }
        }
        return dimensionsForOutputVar;
    }

    /**
     * Utility method to copy the attributes from the <code>inputVariable</code> to the
     * <code>outputVariable</code>, overriding/adding the specified attributes.
     *
     * @param inputVariable            the source variable.
     * @param outputVariable           the target variable. This object is modified by this method.
     * @param overrideNetcdfAttributes any attribute overrides to perform.
     */
    static public void copyVariableAttributes(Variable inputVariable,
                                              Variable outputVariable,
                                              Map<String, String> overrideNetcdfAttributes) {

        // Loop through all attributes of the input variable, copying all that are not included in
        // the overrides.
        for (Attribute inputAttribute : inputVariable.getAttributes()) {

            // Ignore any attributes that do not have a value.
            final String attrValue = inputAttribute.getStringValue();
            if (attrValue != null) {

                // Only copy attributes that are not in the overrides.
                final String attrName = inputAttribute.getFullName();
                if (overrideNetcdfAttributes == null ||
                    overrideNetcdfAttributes.get(attrName) == null) {
                    Attribute outputAttribute = new Attribute(new Parameter(attrName, attrValue));
                    outputVariable.addAttribute(outputAttribute);
                }
            }
        }

        // Add the overrides.
        if (overrideNetcdfAttributes != null) {
            for (String name : overrideNetcdfAttributes.keySet()) {
                final String value = overrideNetcdfAttributes.get(name);
                // Ignore if the value is 'NONE'.
                if (value != null && !value.equalsIgnoreCase("NONE")) {
                    Attribute outputAttribute = new Attribute(new Parameter(name, value));
                    outputVariable.addAttribute(outputAttribute);
                }
            }
        }
    }

    // Write the pre-built list of aggregated times to the new output dataset.
    static public void writeTimeData(OutputDataset outputDataset,
                                     Variable timeOutputVariable,
                                     NcAggregateTask task) throws InvalidRangeException,
        IOException {

        final List<NcAggregateTask.TimeInstant> timeInstants = task.getTimeInstants();
        Array aggregatedTimeArray = new ArrayDouble.D1(timeInstants.size());
        for (int i = 0; i < timeInstants.size(); i++) {
            aggregatedTimeArray.setDouble(i, timeInstants.get(i).getValue());
        }
        try {
            int[] offset = new int[1];
            offset[0] = 0;
            outputDataset.write(timeOutputVariable, offset, aggregatedTimeArray);
        } catch (Throwable e) {
            final String message = "Error writing variable "
                + timeOutputVariable.getFullName() + "\n"
                + "output variable shape is " + Arrays.toString(timeOutputVariable.getShape()) + "\n"
                + "array shape is " + Arrays.toString(aggregatedTimeArray.getShape())
                + "array size is " + aggregatedTimeArray.getSize()
                + "variable size is " + timeOutputVariable.getSize();
            throw (e);
        }
    }

    static public void writeNonTimeData(OutputDataset outputDataset,
                                        Variable outputVariable,
                                        Array data) throws InvalidRangeException,
        IOException {

        // Write to the output dataset.
        try {
            int[] offset = new int[1];
            offset[0] = 0;
            outputDataset.write(outputVariable, data);
        } catch (Throwable e) {
            final String message = "Error writing variable "
                + outputVariable.getFullName() + "\n"
                + "output variable shape is " + Arrays.toString(outputVariable.getShape()) + "\n"
                + "array shape is " + Arrays.toString(data.getShape())
                + "array size is " + data.getSize()
                + "variable size is " + outputVariable.getSize();
            logger.error(message, e);
            throw (e);
        }
    }

}





