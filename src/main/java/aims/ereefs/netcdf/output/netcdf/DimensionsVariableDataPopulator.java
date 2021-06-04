package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import aims.ereefs.netcdf.util.dataset.DatasetUtils;
import aims.ereefs.netcdf.util.netcdf.ArrayUtils;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;


/**
 * Helper class for populating the data for dimension variables.
 *
 * @author Aaron Smith
 */
public class DimensionsVariableDataPopulator {

    static protected Logger logger = LoggerFactory.getLogger(DimensionsVariableDataPopulator.class);

    /**
     * Coordinate copying dimension data from the reference dataset to the output dataset.
     */
    static public void populate(NcAggregateTask task,
                                InputDataset referenceDataset,
                                OutputDataset outputDataset,
                                RegularGridMapper regularGridMapper) throws
        IOException, InvalidRangeException {

        // Grab references for easier use.
        OutputDatasetInfo outputDatasetInfo = outputDataset.getOutputDatasetInfo();
        List<Variable> inputDimensionVariables = outputDatasetInfo.inputDimensionVariables;
        List<Variable> outputDimensionVariables = outputDatasetInfo.outputDimensionVariables;

        logger.debug("----- start -----");

        // Copy the data.
        for (int i = 0; i < inputDimensionVariables.size(); i++) {
            final Variable inputVariable = inputDimensionVariables.get(i);
            final String variableName = inputVariable.getShortName();

            logger.debug("  " + variableName);

            // Treat 'time' dimension differently. Note that we already have the complete list
            // of aggregated times, so we can just use that.
            if (variableName.equalsIgnoreCase("time")) {
                DatasetUtils.writeTimeData(
                    outputDataset,
                    outputDimensionVariables.get(i),
                    task
                );
            } else {
                // Treat depth differently because we may have a subset.
                Array data = null;
                final Dimension depthDimension = referenceDataset.findDepthDimension(inputVariable);
                if (depthDimension != null) {
                    data = ArrayUtils.asArray(referenceDataset.getSelectedDepths(depthDimension.getFullName()));
                } else {
                    // The values for any other dimension variable should be contained within the
                    // reference dataset. No need to accumulate across input files.
                    Variable referenceInputVariable = referenceDataset.findVariable(
                        variableName
                    );
                    if (referenceInputVariable == null) {
                        throw new RuntimeException(
                            "Input variable \"" + variableName + "\" cannot be found.");
                    }
                    data = referenceInputVariable.read();
                }
                DatasetUtils.writeNonTimeData(outputDataset, outputDimensionVariables.get(i), data);
            }
        }

        // Handle regridding.
        if (regularGridMapper != null) {
            logger.debug("  latitude");
            outputDataset.write(
                outputDatasetInfo.latitudeVariable,
                regularGridMapper.getOutputLatitudeArray()
            );
            logger.debug("  longitude");
            outputDataset.write(
                outputDatasetInfo.longitudeVariable,
                regularGridMapper.getOutputLongitudeArray()
            );
        }

        logger.debug("----- end -----");
    }

}
