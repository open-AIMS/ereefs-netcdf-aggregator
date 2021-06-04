package aims.ereefs.netcdf.input.netcdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <code>Builder</code> object responsible for instantiating and populating an {@link InputDataset}
 * object.
 *
 * @author Aaron Smith
 */
public class InputDatasetBuilder {

    static protected Logger logger = LoggerFactory.getLogger(InputDatasetBuilder.class);

    /**
     * Factory method to instantiate an {@link InputDataset} from the specified
     * {@code NetcdfDataset}.
     *
     * @param dataset         the underlying <code>NetcdfDataset</code>.
     * @param specifiedDepths the depths specified in the configuration file.
     * @return the instantiated {@link InputDataset}.
     */
    static public InputDataset build(NetcdfDataset dataset,
                                     List<Double> specifiedDepths,
                                     boolean showDepths) {

        // Determine the Time and Depth dimensions.
        CoordinateAxis1D depthCoordinateAxis = null;
        List<Dimension> depthDimensions = new ArrayList<Dimension>();
        Map<String, List<Double>> selectedDepthsByDimension = new HashMap<String, List<Double>>();
        Map<String, Map<Double, Integer>> selectedDepthToIndexMapByDimension =
            new HashMap<String, Map<Double, Integer>>();
        Dimension timeDimension = null;
        for (CoordinateAxis coordinateAxis : dataset.getCoordinateAxes()) {
            switch (coordinateAxis.getAxisType().name().toLowerCase()) {
                case "lat":
                    // Do nothing.
                    break;
                case "lon":
                    // Do nothing.
                    break;
                case "height":
                    depthCoordinateAxis = ((CoordinateAxis1D) coordinateAxis);

                    // The first dimension is the corresponding dimension.
                    final Dimension dimension = depthCoordinateAxis.getDimension(0);
                    final String dimensionName = dimension.getFullName();
                    depthDimensions.add(dimension);

                    // Build a list of depths available for processing.
                    List<Double> availableDepths =
                        Arrays.stream(depthCoordinateAxis.getCoordValues())
                            .boxed()
                            .collect(Collectors.toList());

                    // Define the lists/maps we are building.
                    List<Double> selectedDepths = new ArrayList<Double>();
                    Map<Double, Integer> selectedDepthToIndexMap = new TreeMap<Double, Integer>();

                    // Loop through each available depth, adding any that were
                    // specified, or all if none specified.
                    for (int index = 0; index < availableDepths.size(); index++) {
                        Double depth = availableDepths.get(index);

                        // Include the depth if it was specified, or no depths were specified.
                        if (specifiedDepths.size() == 0 || specifiedDepths.contains(depth)) {
                            selectedDepths.add(depth);
                            selectedDepthToIndexMap.put(depth, index);
                        }
                    }
                    if (showDepths) {
                        logger.debug("Including depths (" + dimensionName + "):");
                        for (Double selectedDepth : selectedDepths) {
                            logger.debug("    " + selectedDepth);
                        }
                        logger.debug("<end of list>");
                    }

                    selectedDepthsByDimension.put(dimensionName, selectedDepths);
                    selectedDepthToIndexMapByDimension.put(dimensionName, selectedDepthToIndexMap);

                    break;
                case "time":
                    // The first dimension is the corresponding dimension.
                    timeDimension = coordinateAxis.getDimension(0);
                    break;
            }
        }

        // Verify parameters before instantiating InputDataset.
        if (depthDimensions.isEmpty()) {
            throw new RuntimeException("Dataset does not have a Depth dimension.");
        }
        if (timeDimension == null) {
            throw new RuntimeException("Dataset does not have a Time dimension.");
        }
        return InputDataset.make(
            dataset,
            depthDimensions,
            timeDimension,
            selectedDepthsByDimension,
            selectedDepthToIndexMapByDimension
        );
    }

}