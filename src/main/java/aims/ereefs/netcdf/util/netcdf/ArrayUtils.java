package aims.ereefs.netcdf.util.netcdf;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import java.util.List;

/**
 * Utilities related to processing Netcdf arrays.
 *
 * @author Aaron Smith
 */
public class ArrayUtils {

    /**
     * Package the list of <code>Double</code> objects as a Netcdf Array.
     */
    static public Array asArray(List<Double> list) {
        Array array = new ArrayDouble.D1(list.size());
        int index = 0;
        for (double item : list) {
            array.setDouble(index, item);
            index++;
        }
        return array;
    }

    /**
     * Package the array of <code>Double</code> objects as a Netcdf Array.
     */
    static public Array asArray(Double[] list) {
        Array array = new ArrayDouble.D1(list.length);
        int index = 0;
        for (double item : list) {
            array.setDouble(index, item);
            index++;
        }
        return array;
    }

    /**
     * Package the NetCdf {@code Array} as a Java {@code Double Array}.
     *
     * @return
     */
    static public Double[] asJavaDoubleArray(Array input) {
        int size = (int) input.getSize();
        Double[] output = new Double[size];
        for (int index = 0; index < size; index++) {
            output[index] = input.getDouble(index);
        }
        return output;
    }

}
