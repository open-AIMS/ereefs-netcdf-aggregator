package aims.ereefs.netcdf.regrid;

import aims.ereefs.netcdf.util.WeightedMeanCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.*;

import java.util.*;

/**
 * Utility class to convert a location on a curved linear grid to the corresponding location on
 * a regular grid. This class uses the cached grid mapping created by {@link RegularGridMapperBuilder}.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class RegularGridMapper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int latitudeCount;
    private int longitudeCount;

    //This maps a point (x,y) on the regular grid to the four closest points on the curved grid
    private Map<Point, IndexWithDistance[]> regularGridToCurvedIndex = new HashMap<>();

    public Map<Point, IndexWithDistance[]> getRegularGridToCurvedIndex() {
        return regularGridToCurvedIndex;
    }

    /**
     * Cached reference to the populated latitude array for use AFTER re-gridding.
     */
    private Array outputLatitudeArray;

    /**
     * Cached reference to the populated longitude array for use AFTER re-gridding.
     */
    private Array outputLongitudeArray;

    public RegularGridMapper(int latitudeCount, int longitudeCount,
                             Array outputLatitudeArray, Array outputLongitudeArray,
                             Map<Point, IndexWithDistance[]> regularGridToCurvedIndex) {
        this.latitudeCount = latitudeCount;
        this.longitudeCount = longitudeCount;
        this.outputLatitudeArray = outputLatitudeArray;
        this.outputLongitudeArray = outputLongitudeArray;
        this.regularGridToCurvedIndex = regularGridToCurvedIndex;
    }

    public int getLatitudeCount() {
        return this.latitudeCount;
    }

    public int getLongitudeCount() {
        return this.longitudeCount;
    }

    // TODO: This is required for building the output dataset, but I think it should come from
    // somewhere else, not here.
    public Array getOutputLatitudeArray() {
        return this.outputLatitudeArray;
    }

    // TODO: This is required for building the output dataset, but I think it should come from
    // somewhere else, not here.
    public Array getOutputLongitudeArray() {
        return this.outputLongitudeArray;
    }

    /**
     * Converts a curved array to regular grid.
     *
     * @param curvedArray should be 2 or 3 dimensions (optional depth, Latitude and longitude)
     *                    it may also have extra single length dimensions at the start
     * @return the converted array as a regular grid.
     */
    public Array curvedToRegular(Array curvedArray) {

        final int[] inputShape = curvedArray.getShape();
        final int numberOfDimensions = inputShape.length;

        // Check that there are at most 3 non-single length dimensions
        for (int i = 0; i < numberOfDimensions - 3; i++) {
            if (inputShape[i] != 1) {
                throw new RuntimeException("CurvedToRegular expects 2 or 3 non single length dimensions. Optional " +
                        "depth, latitude and longitude. Shape is " + Arrays.toString(inputShape) + ".");
            }
        }

        // Determine the shape of the output array, which will include the same Time and Depth
        // values if present, but also the regridded longitude and latitude dimensions.
        int[] outputShape = Arrays.copyOf(inputShape, numberOfDimensions);
        outputShape[numberOfDimensions - 1] = this.longitudeCount;
        outputShape[numberOfDimensions - 2] = this.latitudeCount;

        // retain the shape of the input so we can reshape the converted array.
        int[] convertedShape = Arrays.copyOf(inputShape, numberOfDimensions);
        convertedShape[numberOfDimensions - 1] = this.longitudeCount;
        convertedShape[numberOfDimensions - 2] = this.latitudeCount;
        // apart from latitude and longitude, all other dimensions should be single length
        for (int i = 0; i < numberOfDimensions - 2; i++) {
            convertedShape[i] = 1;
        }

        // Does the input data have a depth dimension? If so, each depth will need to be processed
        // individually.
        Array result = null;
        if (numberOfDimensions >= 3) {

            // The data includes a depth dimension, so each depth will be processed individually.

            // Remove any dimensions before the depth dimension.
            int[] threeDimShape = Arrays.copyOfRange(inputShape, numberOfDimensions - 3, numberOfDimensions);
            final Array threeDimArray = curvedArray.reshapeNoCopy(threeDimShape);
            int[] sliceShape = Arrays.copyOf(threeDimShape, threeDimShape.length);

            // Create an array of 2D arrays.
            sliceShape[0] = 1;
            int[] offsetShape = new int[3];
            offsetShape[1] = 0;
            offsetShape[2] = 0;
            int depthCount = threeDimShape[0];
            Array[] curvedArrays = new Array[depthCount];
            for (int depthIndex = 0; depthIndex < depthCount; depthIndex++) {
                Array depthSlice;
                try {
                    offsetShape[0] = depthIndex;
                    // there seems to be  a bug with Array.section
                    // calling getDouble on the sectioned array gives the same result as calling getDouble on the original
                    // see aims.ereefs.netcdf.BugTest
                    depthSlice = threeDimArray.section(offsetShape, sliceShape);
                    depthSlice = depthSlice.reshape(depthSlice.getShape());

                } catch (InvalidRangeException e) {
                    throw new RuntimeException("Error sectioning array."
                        + "\n Array Dimensions: " + Arrays.toString(threeDimArray.getShape())
                        + "\n offset: " + Arrays.toString(offsetShape)
                        + "\n shape: " + Arrays.toString(sliceShape));

                }
                curvedArrays[depthIndex] = depthSlice;
            }

            Array[] regularArrays = this.curvedToRegular2D(curvedArrays);

            // Reshape each array due to possible problems in NetCDF library.
            for (int index = 0; index < regularArrays.length; index++) {
                regularArrays[index] = regularArrays[index].reshapeNoCopy(convertedShape);
            }

            // Stitch the individual depths back together.
            Array outputArray = Array.factory(curvedArray.getDataType(), outputShape);
            int outputArrayIndex = 0;
            for (Array regularArray : regularArrays) {
                for (int i = 0; i < regularArray.getSize(); i++) {
                    outputArray.setDouble(outputArrayIndex, regularArray.getDouble(i));
                    outputArrayIndex++;
                }
            }
            result = outputArray;

        } else {

            // Only 2 dimensional input data, so simply convert and reshape, and then it's ready
            // to return.
            Array regularArray = this.curvedToRegular2D(curvedArray);
            result = regularArray.reshapeNoCopy(convertedShape);
        }
        return result;

    }

    private Array curvedToRegular2D(Array curvedArray) {
        return curvedToRegular2D(new Array[]{curvedArray})[0];
    }

    /**
     * Projects curved linear arrays onto a 2D regular grid. This method steps through the
     * resulting regular grid, calculating each cells value from the corresponding points on the
     * curved linear grid as it goes.
     *
     * @param inputCurvedArrays - an array to two dimensional arrays for processing simultaneously.
     * @return regular Array
     */
    private Array[] curvedToRegular2D(Array[] inputCurvedArrays) {

        final int depths = inputCurvedArrays.length;
        if (depths == 0) {
            throw new RuntimeException("No inputs received.");
        }

        // Grab a reference array.
        Array referenceInputCurvedArray = inputCurvedArrays[0];
        if (referenceInputCurvedArray.getShape().length != 2) {
            throw new RuntimeException("CurvedToRegular2D expects exactly dimensions. Latitude and longitude." +
                "shape is " + Arrays.toString(referenceInputCurvedArray.getShape()));
        }

        // Build the output arrays for the regular grid.
        int[] regularGridArrayShape = new int[2];
        regularGridArrayShape[0] = this.latitudeCount;
        regularGridArrayShape[1] = this.longitudeCount;
        Array[] outputRegularGridArrays = new Array[depths];
        for (int index = 0; index < depths; index++) {
            outputRegularGridArrays[index] =
                Array.factory(referenceInputCurvedArray.getDataType(), regularGridArrayShape);
        }
        Array referenceOutputRegularGridArray = outputRegularGridArrays[0];
        Index regularGridIndex = referenceOutputRegularGridArray.getIndex();

        // Loop through every cell of the regular grid array to populate it.
        for (int lonIndex = 0; lonIndex < this.longitudeCount; lonIndex++) {
            for (int latIndex = 0; latIndex < this.latitudeCount; latIndex++) {

                // Update the index pointer for the reference 2D array.
                regularGridIndex = regularGridIndex.set(latIndex, lonIndex);

                // For the current point on the regular grid (identified by lat/lon), retrieve a
                // list of points from the curved grid that contribute to the current value.
                final IndexWithDistance[] indexWithDistances =
                    regularGridToCurvedIndex.get(new Point(latIndex, lonIndex));
                if ((indexWithDistances == null) ||
                    (indexWithDistances.length == 0)) {

                    // No associated points from curved grid, so set as NaN.
                    for (int index = 0; index < depths; index++) {
                        outputRegularGridArrays[index].setDouble(regularGridIndex, Double.NaN);
                    }

                } else {

                    for (int index = 0; index < depths; index++) {
                        Double[] values = new Double[indexWithDistances.length];
                        Double[] weights = new Double[indexWithDistances.length];
                        int i = 0;
                        for (IndexWithDistance indexWithDistance : indexWithDistances) {
                            values[i] = inputCurvedArrays[index].getDouble(indexWithDistance.getIndex());
                            weights[i] = indexWithDistance.getWeight();
                            i++;
                        }
                        final double average = WeightedMeanCalculator.calculate(values, weights);
                        outputRegularGridArrays[index].setDouble(regularGridIndex, average);
                    }

                }

            }
        }

        return outputRegularGridArrays;

    }

}
