package aims.ereefs.netcdf.input.netcdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a <code>NetCDF Dataset</code>, providing filtered access to necessary data.
 *
 * @author Aaron Smith
 */
public class InputDataset implements AutoCloseable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cached reference to the underlying {@code NetCDF Dataset}.
     */
    protected NetcdfDataset dataset;

    public NetcdfDataset getDataset() {
        return this.dataset;
    }

    /**
     * Cached reference to the list of {@code Depth} dimensions in the {@link #dataset}.
     */
    protected List<Dimension> depthDimensions = new ArrayList<Dimension>();

    public List<Dimension> getDepthDimensions() {
        return this.depthDimensions;
    }

    /**
     * Returns the index (position) of the {@code Depth} dimension for the specified
     * {@code variable}. Note that the dataset may contain more than one (1) depth dimension, so
     * it is necessary to search for the depth dimension used by the variable. To simplify the
     * logic, this method returns the {@code first} depth dimension found.
     *
     * @param variable the variable for which the {@code Depth} dimension is of interest.
     * @return the index (position) of the {@code Depth} dimension, or {@code -1} if the variable
     * or {@link #dataset} does not have a {@code Depth} dimension.
     */
    public int findDepthDimensionIndex(Variable variable) {
        final Dimension depthDimension = this.findDepthDimension(variable);
        if (depthDimension != null) {
            return variable.findDimensionIndex(depthDimension.getFullName());
        }
        return -1;
    }

    /**
     * Identifies the {@code Dimension} for the specified {@code Variable} that signifies the
     * {@code depth dimension}. This method compares all of the {@code Dimensions} in the
     * {@code Variable} to the list of {@link #depthDimensions}, returning the first
     * {@code Dimension} that matches.
     *
     * @param variable the variable for which the {@code Depth} dimension is of interest.
     * @return the {@code depth dimension} if found, or {@code null} otherwise.
     */
    public Dimension findDepthDimension(Variable variable) {
        for (final Dimension dimension : variable.getDimensions()) {
            for (final Dimension depthDimension : this.depthDimensions) {
                if (dimension.getFullName().equalsIgnoreCase(depthDimension.getFullName())) {
                    return dimension;
                }
            }
        }
        return null;
    }

    /**
     * Cached reference to the {@code Time} dimension in the {@link #dataset}.
     */
    protected Dimension timeDimension = null;

    public Dimension getTimeDimension() {
        return this.timeDimension;
    }

    /**
     * Returns the index (position) of the {@code Time} dimension for the specified
     * {@code variable}.
     *
     * @param variable the variable for which the {@code Time} dimension is of interest.
     * @return the index (position) of the {@code Time} dimension, or {@code -1} if the variable
     * or {@link #dataset} does not have a {@code Time} dimension.
     */
    public int findTimeDimensionIndex(Variable variable) {
        if (this.timeDimension != null) {
            return variable.findDimensionIndex(this.timeDimension.getFullName());
        }
        return -1;
    }

    /**
     * A list of depths supported by the {@link #dataset} that match the depths of interest in the
     * configuration file. Each depth dimension present in the dataset will have an entry bound
     * to the name of the dimension (eg: "k").
     */
    protected Map<String, List<Double>> selectedDepthsByDimension = new HashMap<String, List<Double>>();

    public List<Double> getSelectedDepths(Variable variable) {
        final Dimension depthDimension = this.findDepthDimension(variable);
        if (depthDimension != null) {
            return this.getSelectedDepths(depthDimension.getFullName());
        }
        return null;
    }

    public List<Double> getSelectedDepths(String depthDimensionName) {
        if (this.selectedDepthToIndexMapByDimension.containsKey(depthDimensionName)) {
            return this.selectedDepthsByDimension.get(depthDimensionName);
        }
        return null;
    }

    /**
     * A mapping of {@code selectedDepths} (see {@link #selectedDepthsByDimension} to their actual
     * index within the {@link #dataset}. For example, the input dataset might have depths
     * (-8, -6, -4, -2), but we only want to copy (-6, -2). This map will then have the entries
     * [(-6, 1), (-2, 3)] which represents the zero-index (position) of the depths being copied
     * (ie: depth 2 and 4). This is important for filtering depths when reading/writing.
     * <p>
     * Note that the order in which the depths are stored in this {@code Map} are important, so
     * a {@code TreeMap} should be used.
     */
    protected Map<String, Map<Double, Integer>> selectedDepthToIndexMapByDimension =
        new HashMap<String, Map<Double, Integer>>();

    public Map<Double, Integer> getSelectedDepthToIndexMap(Variable variable) {
        final Dimension depthDimension = this.findDepthDimension(variable);
        if (depthDimension != null) {
            return this.selectedDepthToIndexMapByDimension.get(depthDimension.getFullName());
        }
        return null;
    }

    /**
     * Constructor.
     */
    protected InputDataset() {
    }

    /**
     * Factory method to instantiate an {@link InputDataset}.
     *
     * @param dataset the underlying <code>NetcdfDataset</code>.
     * @return a new {@link InputDataset}.
     */
    static public InputDataset make(NetcdfDataset dataset,
                                    List<Dimension> depthDimensions,
                                    Dimension timeDimension,
                                    Map<String, List<Double>> selectedDepthsByDimension,
                                    Map<String, Map<Double, Integer>> selectedDepthToIndexMapByDimension) {
        InputDataset inputDataset = new InputDataset();
        inputDataset.dataset = dataset;
        inputDataset.depthDimensions = depthDimensions;
        inputDataset.timeDimension = timeDimension;
        inputDataset.selectedDepthsByDimension.putAll(selectedDepthsByDimension);
        inputDataset.selectedDepthToIndexMapByDimension.putAll(selectedDepthToIndexMapByDimension);

        return inputDataset;
    }

    /**
     * Find the variable with a matching <code>shortName</code> from {@link #getVariables()}.
     * <p>
     * This method throws a RuntimeException if the variable is not found as that is an error
     * with the coding.
     *
     * @param shortName the value to search for.
     * @return the matching <code>Variable</code>.
     * @throws RuntimeException if a <code>Variable</code> is not found.
     */
    public Variable findVariable(String shortName) throws RuntimeException {
        for (Variable variable : this.getVariables()) {
            if (variable.getShortName().equals(shortName)) {
                return variable;
            }
        }
        return null;
    }

    /**
     * Find the list of variables matching the supplied <code>shortNames</code> by invoking the
     * {@link #findVariable(String)} method for each <code>shortName</code>.
     *
     * @param shortNames the list of values to search for.
     * @return a list of matching <code>Variable</code>s.
     * @throws RuntimeException propagated from {@link #findVariable(String)} if a
     *                          <code>shortName</code> is not found.
     */
    public List<Variable> findVariables(List<String> shortNames) throws RuntimeException {
        List<Variable> variables = new ArrayList<>();
        for (String shortName : shortNames) {
            Variable variable = this.findVariable(shortName);
            if (variable != null) {
                variables.add(variable);
            }
        }
        return variables;
    }

    /**
     * Returns a list of <code>Dimensions</code> from the underlying {@link #dataset}.
     *
     * @return the list of {@code Dimensions}.
     */
    public List<Dimension> getDimensions() {
        return this.dataset.getDimensions();
    }

    /**
     * Returns a list of <code>GlobalAttributes</code> from the underlying {@link #dataset}.
     *
     * @return the list of <code>GlobalAttributes</code>.
     */
    public List<Attribute> getGlobalAttributes() {
        return this.dataset.getGlobalAttributes();
    }

    /**
     * Returns a list of <code>Variables</code> from the underlying {@link #dataset}.
     *
     * @return the list of <code>Variables</code>.
     */
    public List<Variable> getVariables() {
        return this.dataset.getVariables();
    }

    /**
     * Helper method to return the variable that contains {@code Latitude} data.
     */
    public Variable getLatitudeVariable() {
        return this.findVariable("latitude");
    }


    /**
     * Helper method to return the variable that contains {@code Longitude} data.
     */
    public Variable getLongitudeVariable() {
        return this.findVariable("longitude");
    }

    /**
     * Close the underlying {@link #dataset} and clear the reference.
     */
    public void close() {
        try {
            this.dataset.close();
            this.dataset = null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to close dataset.", e);
        }
    }

    /**
     * Convenience method for writing debug information to the specified {@code logger}.
     *
     * @param logger reference to the {@code logger} to use for writing the debug information.
     */
    public void dump(Logger logger) {
        logger.debug("----- Start DatasetInfo Dump -----");
        logger.debug("dataset: " + this.dataset.getReferencedFile().getLocation());

        logger.debug("--- Start Dimensions ---");
        for (Dimension dimension : this.dataset.getDimensions()) {
            logger.debug(dimension.toString());
        }
        logger.debug("--- End Dimensions ---");

        logger.debug("--- Start Variables ---");
        for (Variable variable : this.dataset.getVariables()) {
            logger.debug(variable.getShortName() +
                " (" + variable.getDataType().name().toLowerCase() + ")");
            for (Dimension dimension : variable.getDimensions()) {
                logger.debug("    " + dimension.toString());
            }
        }
        logger.debug("--- End Variables ---");

        logger.debug("----- End DatasetInfo Dump -----");
    }

}