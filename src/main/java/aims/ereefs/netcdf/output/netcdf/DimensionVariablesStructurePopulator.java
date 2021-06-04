package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import aims.ereefs.netcdf.util.dataset.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Helper class to populate the output dataset with the dimensions from the input dataset. Although
 * an output dataset could be the result of multiple input datasets, this class assumes that all
 * input datasets contain the full list of dimensions required by the output dataset. These
 * normally are:
 * <p>
 * - aggregated time is used, though this is populated later.
 * - "k" (or "depth") is replaced with only those depths of interest.
 * - if grid mapping is to be performed, "i" and "j" are mapped to "latitude" and "longitude".
 * Utilities for accessing and/or processing Netcdf Datasets.
 * <p>
 * The following steps are required to create a new NetCDF file:
 *
 * <ul>
 * <li>Copy Global Attributes.</li>
 * <li>Copy the dimensions. Note that dimensions themselves do not contain any data. The data is
 * actually contained in the <code>DimensionVariables</code>.</li>
 * <li>Copy the variables structure.</li>
 * <li>Copy the Dimension Variables structure.</li>
 * <li>Copy the Dimension Variables data.</li>
 * </ul>
 *
 * @author Aaron Smith
 */
public class DimensionVariablesStructurePopulator {

    static protected Logger logger = LoggerFactory.getLogger(DimensionVariablesStructurePopulator.class);

    /**
     * Coordinate copying dimension data from the input dataset to the output dataset.
     */
    static public void populate(OutputDataset outputDataset,
                                InputDataset referenceDataset,
                                Map<String, Dimension> inputDimensionNameToOutputDimensionMap,
                                RegularGridMapper regularGridMapper)
        throws IOException, InvalidRangeException {

        // Grab references for easier use.
        OutputDatasetInfo outputDatasetInfo = outputDataset.getOutputDatasetInfo();

        logger.debug("----- start -----");

        // Is regridding specified?
        final boolean isRegridded = regularGridMapper != null;

        // Loop through all variables from the input dataset, processing only dimension variables.
        // A dimension variable is a variable that contains data for a dimension, and is flagged by
        // "isCoordinateVariable". When a dimension variable is found, create the corresponding
        // variable in the output dataset and copy it's attributes.
        for (Variable inputVariable : referenceDataset.getVariables()) {
            if (inputVariable instanceof CoordinateAxis) {

                // Process all dimension variables if the dataset is NOT being regridded. If it IS
                // being regridded, process if the dimension variable is not "latitude" or
                // "longitude".
                if (
                    (!isRegridded) ||
                        (
                            !inputVariable.getFullName().equalsIgnoreCase("latitude") &&
                                !inputVariable.getFullName().equalsIgnoreCase("longitude")
                        )
                ) {
                    logger.debug("  " + inputVariable.getFullName());
                    List<Dimension> outputDimensions =
                        DatasetUtils.findOutputDimensionsByInputVariableName(
                            inputVariable,
                            inputDimensionNameToOutputDimensionMap
                        );
                    if (outputDimensions.size() > 0) {
                        Variable outputVariable =
                            outputDataset.addVariable(
                                inputVariable.getShortName(),
                                DataType.DOUBLE,
                                outputDimensions
                            );
                        DatasetUtils.copyVariableAttributes(inputVariable, outputVariable, null);
                        outputDatasetInfo.inputDimensionVariables.add(inputVariable);
                        outputDatasetInfo.outputDimensionVariables.add(outputVariable);
                    }
                }
            }
        }

        // in the curved grid the variables latitude and longitude have two dimensions, but in
        // the regular grid they have only one dimension so copyVariableStructure does not work
        // for these variables. The following handles creating the latitude and longitude
        // variables for regular grids.
        if (isRegridded) {
            // Make regular Lat variable.
            List<Dimension> latDimensions = new ArrayList<Dimension>();
            Dimension jDimension = inputDimensionNameToOutputDimensionMap.get("j");
            if (jDimension == null) {
                throw new RuntimeException("Dimension could not be found for \"j\".");
            }
            latDimensions.add(jDimension);
            Variable outputLatitudeVariable =
                outputDataset.addVariable(
                    "latitude",
                    DataType.DOUBLE,
                    latDimensions
                );
            Variable inputLatitudeVariable = referenceDataset.getLatitudeVariable();
            DatasetUtils.copyVariableAttributes(inputLatitudeVariable, outputLatitudeVariable, null);
            // Cache the reference.
            outputDatasetInfo.latitudeVariable = outputLatitudeVariable;

            // Make regular Lon variable.
            List<Dimension> lonDimensions = new ArrayList<Dimension>();
            Dimension iDimension = inputDimensionNameToOutputDimensionMap.get("i");
            if (iDimension == null) {
                throw new RuntimeException("Dimension could not be found for \"i\".");
            }
            lonDimensions.add(iDimension);
            Variable outputLongitudeVariable =
                outputDataset.addVariable(
                    "longitude",
                    DataType.DOUBLE,
                    lonDimensions
                );
            Variable inputLongitudeVariable = referenceDataset.getLongitudeVariable();
            DatasetUtils.copyVariableAttributes(inputLongitudeVariable, outputLongitudeVariable, null);

            // Cache the reference.
            outputDatasetInfo.longitudeVariable = outputLongitudeVariable;
        }
        logger.debug("----- end -----");
    }

}
