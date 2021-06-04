package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Dimension;

import java.util.HashMap;
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
 *
 * @author Aaron Smith
 */
public class DimensionsPopulator {

    static protected Logger logger = LoggerFactory.getLogger(DimensionsPopulator.class);

    /**
     * Populates the {@link OutputDataset} with the dimensions from the
     * {@link InputDataset referenceDataset}. Since the {@code X} and {@code Y} dimensions can
     * change when regridding, this method returns a map binding the name of the input dimension
     * to the corresponding output dimension.
     */
    static public Map<String, Dimension> populate(OutputDataset outputDataset,
                                                  InputDataset referenceDataset,
                                                  RegularGridMapper regularGridMapper) {

        final Map<String, Dimension> inputDimensionNameToOutputDimensionMap = new HashMap<String, Dimension>();

        // Loop through each dimension from the reference dataset, but temporarily ignore spatial
        // dimensions ('i' and 'j').
        logger.debug("----- start -----");
        for (final Dimension inputDimension : referenceDataset.getDimensions()) {
            final String inputDimensionName = inputDimension.getFullName();

            // Identify the default length of the dimensions' data.
            int length = inputDimension.getLength();

            // Time and Depth are handled differently because they can differ. Input data can
            // be temporally aggregated, resulting in fewer time instants in the output dataset,
            // and depths can be filtered.

            // Is the current dimension the time dimension?
            if (inputDimension.getFullName().equalsIgnoreCase(referenceDataset.getTimeDimension().getFullName())) {
                // Create an unlimited dimension for time because we may append data from more than
                // one input file.
                logger.info("  " + inputDimensionName + " -> " + inputDimensionName);
                final Dimension outputDimension = outputDataset.addUnlimitedDimension(inputDimensionName);
                inputDimensionNameToOutputDimensionMap.put(inputDimensionName, outputDimension);

            } else {

                // Handle if this is a depth dimension.
                for (Dimension depthDimension : referenceDataset.getDepthDimensions()) {
                    if (inputDimension.getFullName().equalsIgnoreCase(depthDimension.getFullName())) {
                        List<Double> selectedDepths = referenceDataset.getSelectedDepths(inputDimensionName);
                        if (selectedDepths == null) {
                            length = 0;
                        } else {
                            length = selectedDepths.size();
                        }
                    }
                }

                // Temporarily ignore the spatial dimensions ('i' and 'j') if we are converting to
                // a regular grid, otherwise write the new dimension.
                if ((regularGridMapper == null) || (!inputDimensionName.equals("i") && !inputDimensionName.equals("j"))) {
                    if (length > 0) {
                        logger.info("  " + inputDimensionName + " -> " + inputDimensionName);
                        final Dimension dimension = outputDataset.addDimension(inputDimensionName, length);
                        inputDimensionNameToOutputDimensionMap.put(inputDimensionName, dimension);
                    }
                }
            }
        }

        // If we are converting to a regular grid we have latitude and longitude
        // instead of i and j we skipped them before
        if (regularGridMapper != null) {
            logger.info("  j -> latitude");
            final Dimension latDimension = outputDataset.addDimension(
                "latitude",
                regularGridMapper.getLatitudeCount()
            );
            inputDimensionNameToOutputDimensionMap.put("j", latDimension);

            logger.info("  i -> longitude");
            final Dimension lonDimension = outputDataset.addDimension(
                "longitude",
                regularGridMapper.getLongitudeCount()
            );
            inputDimensionNameToOutputDimensionMap.put("i", lonDimension);
        }

        logger.debug("----- end -----");

        return inputDimensionNameToOutputDimensionMap;
    }

}

