package aims.ereefs.netcdf.grid.regular;

import aims.ereefs.netcdf.regrid.RegularGridMapper;
import org.assertj.core.api.Assertions;
import ucar.ma2.Array;
import ucar.ma2.Index;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * Tests for the {@link RegularGridMapper} class.
 *
 * @author Aaron Smith
 */
public class RegularGridMapperTestUtils {

    static public void validateRegularGridMapper(RegularGridMapper regularGridMapper) {
        Array inputArray = CurvilinearTestData.timeSlice;

        Array outputArray = regularGridMapper.curvedToRegular(inputArray);
        double[] lonValues = CurvilinearTestData.lonValues;
        double[] latValues = CurvilinearTestData.latValues;

        // Verify the size of the output array, and check the values within each corner are within
        // expected deltas.
        int[] outputShape = outputArray.getShape();
        Assertions
            .assertThat(outputArray.getSize())
            .isEqualTo(outputShape[1] * outputShape[2] * outputShape[3]);
        Index outputIndex = outputArray.getIndex();
        Index timeSliceIndex = inputArray.getIndex();

        // Depth 0.
        double outputValue = outputArray.getDouble(outputIndex.set(0, 0, 0, 0));
        double inputValue = inputArray.getDouble(timeSliceIndex.set(0, 0, 0, 0));
        Assertions
            .assertThat(outputValue)
            .isCloseTo(inputValue, within(1.0));
        outputValue = outputArray.getDouble(outputIndex.set(0, 0, outputShape[2] - 1, outputShape[3] - 1));
        inputValue = inputArray.getDouble(timeSliceIndex.set(0, 0, lonValues.length - 1, latValues.length - 1));
        Assertions
            .assertThat(outputValue)
            .isCloseTo(inputValue, withinPercentage(15.0));

        // Depth 1.
        outputValue = outputArray.getDouble(outputIndex.set(0, 1, 0, 0));
        inputValue = inputArray.getDouble(timeSliceIndex.set(0, 1, 0, 0));
        Assertions
            .assertThat(outputValue)
            .isCloseTo(inputValue, within(1.0));
        outputValue = outputArray.getDouble(outputIndex.set(0, 1, outputShape[2] - 1, outputShape[3] - 1));
        inputValue = inputArray.getDouble(timeSliceIndex.set(0, 1, lonValues.length - 1, latValues.length - 1));
        Assertions
            .assertThat(outputValue)
            .isCloseTo(inputValue, withinPercentage(15.0));

    }

}
