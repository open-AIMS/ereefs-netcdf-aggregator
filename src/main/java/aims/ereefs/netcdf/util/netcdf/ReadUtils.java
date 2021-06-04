package aims.ereefs.netcdf.util.netcdf;

import io.prometheus.client.Gauge;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utilities related to reading data from a NetCDF file.
 *
 * @author Aaron Smith
 */
public class ReadUtils {

    protected static final Gauge apmDatasetReadBytes = Gauge.build()
        .name("ncaggregate_dataset_read_bytes")
        .help("Total size (bytes) of data read from a NetCDF dataset.")
        .register();
    protected static final Gauge apmDatasetReadDuration = Gauge.build()
        .name("ncaggregate_dataset_read_duration_seconds")
        .help("Time taken (seconds) to read data from a NetCDF dataset.")
        .register();

    /**
     * A wrapper function that reads a slice, based on the specified shape/offset, for a single
     * variable.
     *
     * @param variable    the variable from which to read the data.
     * @param sliceShape  a shape that defines the data to read.
     * @param sliceOffset the offset of the slice to be read.
     * @return an array of data from the variable.
     */
    static public Array readData(Variable variable,
                                 int[] sliceShape,
                                 int[] sliceOffset) {
        Array sliceArray = null;
        try {
            Gauge.Timer durationTimer = apmDatasetReadDuration.startTimer();
            sliceArray = variable.read(sliceOffset, sliceShape);
            durationTimer.setDuration();
            apmDatasetReadBytes.inc(sliceArray.getDataType().getSize() * sliceArray.getSize());
        } catch (Throwable e) {
            throw new RuntimeException(
                "Error reading data (shape: " + Arrays.toString(sliceShape) + ", offset: " +
                    Arrays.toString(sliceOffset) + ") for variable \"" + variable.getFullName() +
                    "\" (shape: " + Arrays.toString(variable.getShape()) + ")", e);
        }
        return sliceArray;
    }

    /**
     * Reads a single time slice from the specified variables. This method assumes the variables
     * do not have a depth dimension, and that the first dimension is {@code time}.
     *
     * @param variables       variables from which to read the data.
     * @param timeIndexOffset the offset of the time slice to be read.
     * @return a list of Array objects, where each Array represents a single time slice for a single
     * variable.
     */
    static public List<Double[]> readSingleTimeSlice(List<Variable> variables,
                                                     int timeIndexOffset) {

        // Instantiate the return variable.
        List<Double[]> arrays = new ArrayList<>();

        // Loop through each input variable to read data and add it to the array.
        for (Variable variable : variables) {
            arrays.add(ReadUtils.readSingleTimeSlice(
                variable, timeIndexOffset));
        }
        return arrays;

    }

    /**
     * Reads a single time slice from the specified variable. This method assumes the variable
     * does not have a depth dimension, and that the first dimension is {@code time}.
     *
     * @param variable        the variable from which to read data.
     * @param timeIndexOffset the offset of the time slice to be read.
     * @return an array of data representing a single time slice for a single variable.
     */
    static public Double[] readSingleTimeSlice(Variable variable,
                                               int timeIndexOffset) {

        // Prepare the shape to read. This is the shape of the data for the variable, with the time
        // dimension set to 1 so only a single time slice is read.
        int[] shape = variable.getShape();
        shape[0] = 1;

        // Prepare the offset to read. Only the time dimension is modified.
        int[] offset = new int[]{timeIndexOffset, 0, 0};

        // Read the data.
        return ArrayUtils.asJavaDoubleArray(ReadUtils.readData(variable, shape, offset));

    }

    /**
     * Reads a single time slice from the specified variables. If any depth slices are specified
     * (ie: {@code selectedDepthToIndexMap} is not empty), then result will be filtered for those
     * depths, otherwise all depths will be included. This method assumes that the first dimension
     * is {@code time}, and the second dimension is {@code depth}.
     *
     * @param variables               variables from which to read data.
     * @param timeIndexOffset         the offset of the time slice to be read.
     * @param selectedDepthToIndexMap a map of depths that have been selected for copy, and its
     *                                corresponding index in the dataset.
     * @return a list of Array objects, where each Array represents a single time slice for a single
     * variable, filtered by any specified depths.
     */
    static public List<Double[]> readSingleTimeSlice(List<Variable> variables,
                                                     int timeDimensionIndex,
                                                     int depthDimensionIndex,
                                                     int timeIndexOffset,
                                                     List<Double> selectedDepthsToProcess,
                                                     Map<Double, Integer> selectedDepthToIndexMap) {

        // Instantiate the return variable.
        List<Double[]> arrays = new ArrayList<>();

        // Loop through each input variable to read data and add it to the array.
        for (Variable variable : variables) {
            arrays.add(
                ReadUtils.readSingleTimeSlice(
                    variable,
                    timeDimensionIndex,
                    depthDimensionIndex,
                    timeIndexOffset,
                    selectedDepthsToProcess,
                    selectedDepthToIndexMap
                )
            );
        }
        return arrays;

    }

    /**
     * Reads a single time slice from the single specified variable. Filter depths if
     * {@code selectedDepthToIndexMap} is not empty), otherwise return data for all depths. This
     * method assumes that the first dimension is {@code time}, and the second dimension is
     * {@code depth}.
     *
     * @param variable                the variable from which to read data.
     * @param timeIndexOffset         the offset of the time slice to be read.
     * @param selectedDepthToIndexMap a map of depths that have been selected for copy, and its
     *                                corresponding index in the dataset.
     * @return an array of data representing a single time slice for a single variable, filtered by
     * any specified depths.
     */
    static public Double[] readSingleTimeSlice(Variable variable,
                                               int timeDimensionIndex,
                                               int depthDimensionIndex,
                                               int timeIndexOffset,
                                               List<Double> selectedDepthsToProcess,
                                               Map<Double, Integer> selectedDepthToIndexMap) {

        if (selectedDepthToIndexMap.isEmpty()) {
            throw new RuntimeException("No depths defined. This method should not have been invoked.");
        }

        // Prepare the shape to read. This is the shape of the data for the variable, with the time
        // dimension set to 1 so only a single time slice is read.
        int[] shape = variable.getShape();
        shape[timeDimensionIndex] = 1;

        // Prepare the offset to read. Only the time dimension is modified.
        int[] offset = new int[]{0, 0, 0, 0};
        offset[timeDimensionIndex] = timeIndexOffset;

        // Read the data.
        Array timeSliceArray = ReadUtils.readData(variable, shape, offset);

        // Filter on depths if required.
        if (!selectedDepthToIndexMap.isEmpty()) {

            int[] depthSliceShape = timeSliceArray.getShape();
            depthSliceShape[depthDimensionIndex] = 1;

            // Loop through the depths of interest, reading only those depths, and then combine
            // the results into a single array.
            List<Array> depthSlices = new ArrayList<>();
            for (Double selectedDepth : selectedDepthsToProcess) {
                int depthIndex = selectedDepthToIndexMap.get(selectedDepth);
                int[] depthOffset = new int[]{0, 0, 0, 0};
                depthOffset[depthDimensionIndex] = depthIndex;
                try {
                    Array depthSliceArray = timeSliceArray.section(depthOffset, depthSliceShape);
                    depthSlices.add(depthSliceArray.reshape(depthSliceArray.getShape()));
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            }

            // Combine the retrieved slices into a single slice.
            int[] combinedArrayShape = timeSliceArray.getShape();
            combinedArrayShape[depthDimensionIndex] = depthSlices.size();
            Array combinedArray = new ArrayDouble.D4(combinedArrayShape[0], combinedArrayShape[1],
                combinedArrayShape[2], combinedArrayShape[3]);
            int combinedArrayIndex = 0;
            for (Array depthSliceArray : depthSlices) {
                for (int i = 0; i < depthSliceArray.getSize(); i++) {
                    combinedArray.setDouble(combinedArrayIndex, depthSliceArray.getDouble(i));
                    combinedArrayIndex++;
                }
            }

            // Add the combined array to the list.
            return ArrayUtils.asJavaDoubleArray(combinedArray);

        } else {

            return ArrayUtils.asJavaDoubleArray(timeSliceArray);

        }

    }

    static public Double[] readSingleTimeSliceByDepth(Variable variable,
                                                      int timeIndexOffset,
                                                      Map<Double, Integer> selectedDepthToIndexMap) {

        if (selectedDepthToIndexMap.isEmpty()) {
            throw new RuntimeException("No depths defined. This method should not have been invoked.");
        }

        // Prepare the shape to read. This is the shape of the data for the variable, with the time
        // and depth dimensions set to 1 so only a single time/depth slice is read at once.
        int[] shape = variable.getShape();
        shape[0] = 1;
        shape[1] = 1;

        // Loop through the depths of interest, reading only those depths.
        int combinedSize = 0;
        List<Double[]> depthSlices = new ArrayList<>();
        for (Double selectedDepth : selectedDepthToIndexMap.keySet()) {
            int depthIndex = selectedDepthToIndexMap.get(selectedDepth);

            // Prepare the offset to read. Only the time and depth dimensions are modified.
            int[] offset = new int[]{timeIndexOffset, depthIndex, 0, 0};

            final Array depthSliceArray = ReadUtils.readData(variable, shape, offset);
            combinedSize += depthSliceArray.getSize();
            depthSlices.add(ArrayUtils.asJavaDoubleArray(depthSliceArray));
        }

        // Combine the retrieved slices into a single slice.
        int combinedIndex = 0;
        Double[] combinedArray = new Double[combinedSize];
        for (Double[] depthSlice : depthSlices) {
            for (int index = 0; index < depthSlice.length; index++) {
                combinedArray[combinedIndex + index] = depthSlice[index];
            }
            combinedIndex += depthSlice.length;
        }

        depthSlices.clear();

        return combinedArray;

    }

}
