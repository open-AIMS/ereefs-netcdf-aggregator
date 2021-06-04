package aims.ereefs.netcdf.util.netcdf;

import aims.ereefs.netcdf.output.netcdf.OutputDataset;
import aims.ereefs.netcdf.output.netcdf.OutputDatasetInfo;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.util.Arrays;
import java.util.List;

/**
 * Utilities related to writing data to a NetCDF file.
 *
 * @author Aaron Smith
 */
public class WriteUtils {
    static protected Logger logger = LoggerFactory.getLogger(WriteUtils.class);

    /**
     * Write a single time slice to the output file.
     */
    static public void writeSlice(OutputDataset outputDataset,
                                  NcAggregateProductDefinition.SummaryOperator summaryOperatorDefinition,
                                  List<Array> arrays,
                                  int timeOffset,
                                  int depthOffset) {

        OutputDatasetInfo outputDatasetInfo = outputDataset.getOutputDatasetInfo();

        // Build the offset, which should be zero in all dimensions except time and depth (if
        // exists).
        final String referenceOutputVariableName =
            summaryOperatorDefinition.getOutputVariables().get(0).getAttributes().get("short_name");
        final Variable referenceOutputVariable =
            outputDatasetInfo.outputVariableMap.get(referenceOutputVariableName);
        final int numDimensions = referenceOutputVariable.getShape().length;
        final int timeDimensionIndex = referenceOutputVariable.findDimensionIndex("time");
        int[] sliceOffset = new int[numDimensions];
        for (int i = 0; i < numDimensions; i++) {
            sliceOffset[i] = 0;
        }
        sliceOffset[timeDimensionIndex] = timeOffset;

        int outputDepthDimensionIndex = referenceOutputVariable.findDimensionIndex("k");
        if (outputDepthDimensionIndex != -1) {
            sliceOffset[outputDepthDimensionIndex] = depthOffset;
        }

        // Handle each output variable separately. Convert to RegularGrid before write if required.
        int outputVariableIndex = 0;
        for (Array array : arrays) {
            Array arrayToWrite;
            final String outputVariableName =
                summaryOperatorDefinition.getOutputVariables().get(outputVariableIndex)
                    .getAttributes().get("short_name");
            Variable outputVariable = outputDatasetInfo.outputVariableMap.get(outputVariableName);
            try {
                if (logger.isTraceEnabled()) {
                    boolean isDataFound = false;
                    int searchIndex = 0;
                    while (!isDataFound && searchIndex < array.getSize()) {
                        isDataFound = !Double.isNaN(array.getDouble(searchIndex));
                        searchIndex++;
                    }
                    if (isDataFound) {
                        logger.debug("Data found.");
                    }
                }
                outputDataset.write(outputVariable, sliceOffset, array);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error writing variable " + outputVariable.getDescription() +
                        "\n Variable Shape" + Arrays.toString(outputVariable.getShape()) +
                        "\n sliceOffset = " + Arrays.toString(sliceOffset) +
                        "\n Output Array Shape" + array.shapeToString(), e);
            }

            outputVariableIndex++;
        }

        outputDataset.flush();

    }

}
