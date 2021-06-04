package aims.ereefs.netcdf.output.netcdf;

import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Value object used to cache data while building an {@link OutputDataset}.
 *
 * @author Aaron Smith
 */
public class OutputDatasetInfo {

    /**
     * Cached reference to a <code>DateUnit</code> that can be used for converting dates/times.
     */
    public DateUnit dateUnit;

    /**
     * Cached list links {@link SummaryOperatorVariables} objects to the input and output variables they relate to.
     */
    public List<SummaryOperatorVariables> summaryOperatorVariablesList = new ArrayList<>();

    /**
     * Cached list of variables in input/reference dataset that contain dimensional data. This would
     * include time, depth (k), and i/j (lat/lon).
     */
    public List<Variable> inputDimensionVariables = new ArrayList<>();

    /**
     * Cached list of variables in output dataset that contain dimensional data.
     */
    public List<Variable> outputDimensionVariables = new ArrayList<>();

    /**
     * Cached reference to the output variable that represents the <code>Longitude</code> data.
     */
    public Variable longitudeVariable;

    /**
     * Cached reference to the output variable that represents the <code>Latitude</code> data.
     */
    public Variable latitudeVariable;

    /**
     * List of output variables, bound to the short name of the variable.
     */
    public Map<String, Variable> outputVariableMap = new TreeMap<>();

}
