package aims.ereefs.netcdf.grid.regular;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;

/**
 * Simple static class that instantiates curvilinear data for test cases.
 *
 * @author Aaron Smith
 */
public class CurvilinearTestData {

    /**
     * The longitudes of the cells in the grid.
     */
    final static public double[] lonValues = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};

    /**
     * The latitudes of the cells in the grid.
     */
    final static public double[] latValues = new double[]{11.0, 12.0, 13.0, 14.0, 15.0};

    /**
     * The array representing the longitudes of the cells in the grid. This array is populated by
     * the static class initialiser and is based on {@link #lonValues} and {@link #latValues}.
     */
    static public Array longitudeArray;

    /**
     * The array representing the latitudes of the cells in the grid. This array is populated by
     * the static class initialiser and is based on {@link #lonValues} and {@link #latValues}.
     */
    static public Array latitudeArray;

    /**
     * The depths contained in the {@link #timeSlice} input data.
     */
    // The depths to be found in the input data.
    final static public double[] depths = new double[]{-1.5, -17.75};

    /**
     * An array of data representing input data for a single time slice based on the
     * {@link #longitudeArray}, {@link #latitudeArray} and {@link #depths}. This array is populated
     * by the static class initialiser.
     */
    static public Array timeSlice = Array.factory(DataType.DOUBLE, new int[]{
        1,
        depths.length,
        lonValues.length,
        latValues.length
    });

    static {

        // Populate the LongitudeArray and LatitudeArray.
        int[] shape = new int[]{lonValues.length, latValues.length};
        longitudeArray = new ArrayDouble(shape);
        latitudeArray = new ArrayDouble(shape);
        Index index = Index.factory(shape);
        for (int lonIndex = 0; lonIndex < lonValues.length; lonIndex++) {
            for (int latIndex = 0; latIndex < latValues.length; latIndex++) {
                index.set(lonIndex, latIndex);
                longitudeArray.setDouble(index, lonValues[lonIndex]);
                latitudeArray.setDouble(index, latValues[latIndex]);
            }
        }

        // Populate the input TimeSlice, which is a 4D array for a single time slice.
        index = timeSlice.getIndex();
        for (int depthIndex = 0; depthIndex < 2; depthIndex++) {
            for (int latIndex = 0; latIndex < latValues.length; latIndex++) {
                for (int lonIndex = 0; lonIndex < lonValues.length; lonIndex++) {

                    // Write the value to the array.
                    timeSlice.setDouble(index.set(0, depthIndex, lonIndex, latIndex),
                        depthIndex * 100.0 + latIndex * 10.0 + lonIndex);

                }
            }
        }


    }

}
