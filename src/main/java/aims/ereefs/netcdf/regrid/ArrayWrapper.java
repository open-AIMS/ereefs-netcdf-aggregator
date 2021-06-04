package aims.ereefs.netcdf.regrid;

import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.*;

/**
 * Helper class for mapping a curved grid to a regular grid. Used for the latitude and longitude
 * arrays.
 *
 * NOTE: This class only handles arrays where the values in the array are unique, such as found in
 * arrays of latitudes and longitudes.
 *
 * @author Greg Coleman
 */
public class ArrayWrapper {

    /**
     * Minimum value from the <code>Array</code> provided to the
     * {@link #ArrayWrapper(Array)} constructor}.
     */
    private double minimumValue;

    /**
     * Maximum value from the <code>Array</code> provided to the
     * {@link #ArrayWrapper(Array)} constructor}.
     */
    private double maximumValue;

    //this is how we can take a range of values and return the corresponding indexes within that range
    private NavigableMap<Double, List<Integer>> valueToIndex = new TreeMap<>();
    private Map<Integer, Double> indexToValue= new HashMap<>();


    /**
     * Overloaded constructor to instantiate the class based on the data read from the specified
     * <code>Variable</code>. This constructor performs a <code>read</code> on the specified
     * <code>Variable</code> and then invokes the alternate constructor
     * {@link #ArrayWrapper(Array)}.
     *
     * @param variable the variable from which to read data.
     * @throws IOException exception potentially thrown if problem occurs reading data from
     * variable.
     */
    public ArrayWrapper(Variable variable) throws IOException {
        this(variable.read());
    }

    /**
     * Primary constructor to instantiate the class with the specified <code>Array</code>. This
     * constructor caches the {@link #minimumValue} and {@link #maximumValue} from the array
     *
     * @param array the array to process and cache.
     */
    public ArrayWrapper(Array array) {
        this.minimumValue = Double.MAX_VALUE;
        this.maximumValue = Double.MAX_VALUE*-1;

        for (int i = 0; i< array.getSize(); i++) {
            final double value = array.getDouble(i);
            if (value < this.minimumValue) {
                this.minimumValue = value;
            }
            if (value > this.maximumValue) {
                this.maximumValue = value;
            }

            // Link the value (ie: the latitude/longitude) to it's position within the array.
            List<Integer> indexes = this.valueToIndex.get(value);
            if (indexes == null) {
                indexes = new ArrayList<>();
                this.valueToIndex.put(value, indexes);
            }
            indexes.add(i);
            this.indexToValue.put(i, value);
        }
    }

    public double getMinimumValue() {
        return this.minimumValue;
    }

    public double getMaximumValue() {
        return this.maximumValue;
    }

    public NavigableMap<Double, List<Integer>> getValueToIndex() {
        return this.valueToIndex;
    }

    public Map<Integer, Double> getIndexToValue() {
        return this.indexToValue;
    }

}
