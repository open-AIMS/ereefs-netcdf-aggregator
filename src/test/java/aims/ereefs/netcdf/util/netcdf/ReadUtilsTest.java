package aims.ereefs.netcdf.util.netcdf;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tests for the {@link ReadUtils} class.
 *
 * @author Aaron Smith
 */
public class ReadUtilsTest {

    /**
     * Name of the test NetCDF file.
     */
    static final public String TEST_FILENAME = "src/test/resources/testdata/input/small.nc";

    /**
     * Helper method that compares the content of two (2) <code>Array</code>s.
     *
     * @param array1 first array.
     * @param array2 second array.
     * @return <code>true</code> if the arrays match, <code>false</code> otherwise.
     */
    private void compareArrays(Array array1, Double[] array2) {
        long size1 = array1.getSize();
        long size2 = array2.length;
        Assertions
            .assertThat(size1)
            .isEqualTo(size2);
        Assertions
            .assertThat(size1)
            .isNotZero();
        for (int i = 0; i < size1; i++) {
            Double value1 = array1.getDouble(i);
            Double value2 = array2[i];
            if (!value1.isNaN() || !value2.isNaN()) {
                Assertions
                    .assertThat(value1)
                    .isEqualTo(value2);
            }
        }
    }


    /**
     * Test the {@link ReadUtils#readSingleTimeSlice(Variable, int, int, int, List, Map)} method using a
     * variable with the depth dimension, test reading ALL depths at once. This test will use the
     * variable {@code temp}, which has four (4) dimensions: 2 (time) x 2 (height) x 180 x 600.
     */
    @Test
    public void testReadSingleTimeSliceAllDepths() {
        try {
            final NetcdfDataset dataset = NetcdfDataset.openDataset(ReadUtilsTest.TEST_FILENAME);
            final Variable variable = dataset.findVariable("temp");
            final List<Variable> variables = new ArrayList<>();
            variables.add(variable);

            // Read all of the data for the variable.
            final int[] allTimeSliceShape = variable.getShape();
            final int[] timeSliceOffset = new int[]{0, 0, 0, 0};
            final Array allTimeSliceArray = variable.read(timeSliceOffset, allTimeSliceShape);
            Assertions
                .assertThat(allTimeSliceArray.getSize())
                .isEqualTo(variable.getSize());

            // Loop through each time slice.
            for (int timeIndex = 0; timeIndex < 2; timeIndex++) {

                // Get the control data.
                final int[] controlOffset = new int[]{timeIndex, 0, 0, 0};
                final int[] controlShape = variable.getShape();
                controlShape[0] = 1;
                final Array tempControlSection = allTimeSliceArray.section(controlOffset,
                    controlShape, (int[]) null);
                // Reshape to fix bug in library resulting in incorrect shape.
                final Array controlSection = tempControlSection.reshape(
                    tempControlSection.getShape());


                // Perform the read.
                final List<Double> selectedDepths = new ArrayList<Double>() {{
                    add(0.5);
                    add(1.5);
                }};
                final Map<Double, Integer> depthToIndexMap = new HashMap<Double, Integer>() {{
                    put(0.5, 0);
                    put(1.5, 1);
                }};
                final List<Double[]> testTimeSliceArrayList = ReadUtils.readSingleTimeSlice(
                    variables,
                    0,
                    1,
                    timeIndex,
                    selectedDepths,
                    depthToIndexMap
                );

                Assertions
                    .assertThat(testTimeSliceArrayList.size())
                    .isEqualTo(1);
                final Double[] testTimeSliceArray = testTimeSliceArrayList.get(0);
                Assertions
                    .assertThat(testTimeSliceArray.length)
                    .isEqualTo(1 * 2 * 180 * 600);

                // Compare to the control.
                this.compareArrays(controlSection, testTimeSliceArray);

            }

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Test the {@link ReadUtils#readSingleTimeSlice(Variable, int, int, int, List, Map)} method using a
     * variable with the depth dimension, test reading individual depths. This test will use the
     * variable {@code temp}, which has four (4) dimensions: 2 (time) x 2 (height) x 180 x 600.
     */
    @Test
    public void testReadSingleTimeSliceIndividualDepths() {
        try {
            final NetcdfDataset dataset = NetcdfDataset.openDataset(ReadUtilsTest.TEST_FILENAME);
            final Variable variable = dataset.findVariable("temp");
            final List<Variable> variables = new ArrayList<>();
            variables.add(variable);

            // Read all of the data for the variable.
            final int[] allTimeSliceShape = variable.getShape();
            final int[] timeSliceOffset = new int[]{0, 0, 0, 0};
            final Array allTimeSliceArray = variable.read(timeSliceOffset, allTimeSliceShape);
            Assertions
                .assertThat(allTimeSliceArray.getSize())
                .isEqualTo(variable.getSize());

            // Loop through each time slice.
            for (int timeIndex = 0; timeIndex < 2; timeIndex++) {

                // Loop through each depth slice.
                for (int depthIndex = 0; depthIndex < 2; depthIndex++) {

                    // Get the control data.
                    final int[] controlOffset = new int[]{timeIndex, depthIndex, 0, 0};
                    final int[] controlShape = variable.getShape();
                    controlShape[0] = 1;
                    controlShape[1] = 1;
                    final Array tempControlSection = allTimeSliceArray.section(controlOffset,
                        controlShape, (int[]) null);
                    // Reshape to fix bug in library resulting in incorrect shape.
                    final Array controlSection = tempControlSection.reshape(
                        tempControlSection.getShape());

                    // Only retrieve one depth at a time.
                    Double depth = depthIndex * 1.0;
                    final List<Double> selectedDepths = new ArrayList<Double>() {{
                        add(depth);
                    }};
                    final Map<Double, Integer> depthToIndexMap = new HashMap<>();
                    depthToIndexMap.put(depth, depthIndex);

                    // Perform the read.
                    final List<Double[]> timeSliceArrayList = ReadUtils.readSingleTimeSlice(
                        variables,
                        0,
                        1,
                        timeIndex,
                        selectedDepths,
                        depthToIndexMap
                    );

                    Assertions
                        .assertThat(timeSliceArrayList.size())
                        .isEqualTo(1);
                    final Double[] singleTimeDepthSlice = timeSliceArrayList.get(0);
                    Assertions
                        .assertThat(singleTimeDepthSlice.length)
                        .isEqualTo(1 * 1 * 180 * 600);

                    // Compare to the control.
                    this.compareArrays(controlSection, singleTimeDepthSlice);

                }
            }

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Test the {@link ReadUtils#readSingleTimeSlice(List, int)} method using a variable without
     * a depth dimension. This test will use the variable {@code wspeed_u}, which has three (3)
     * dimensions: 2 (time) x 180 x 600.
     */
    @Test
    public void testReadSingleTimeSliceNoDepths() {
        try {
            final NetcdfDataset dataset = NetcdfDataset.openDataset(ReadUtilsTest.TEST_FILENAME);
            final Variable variable = dataset.findVariable("wspeed_u");
            final List<Variable> variables = new ArrayList<>();
            variables.add(variable);

            // Read all of the data for the variable.
            final int[] allTimeSliceShape = variable.getShape();
            final int[] timeSliceOffset = new int[]{0, 0, 0};
            final Array allTimeSliceArray = variable.read(timeSliceOffset, allTimeSliceShape);
            Assertions
                .assertThat(allTimeSliceArray.getSize())
                .isEqualTo(variable.getSize());

            // Loop through each time slice.
            for (int timeIndex = 0; timeIndex < 2; timeIndex++) {

                // Get the control data.
                final int[] controlOffset = new int[]{timeIndex, 0, 0};
                final int[] controlShape = variable.getShape();
                controlShape[0] = 1;
                final Array tempControlSection = allTimeSliceArray.section(controlOffset,
                    controlShape, (int[]) null);
                // Reshape to fix bug in library resulting in incorrect shape.
                final Array controlSection = tempControlSection.reshape(
                    tempControlSection.getShape());

                // Perform the read.
                final List<Double[]> testTimeSliceArrayList = ReadUtils.readSingleTimeSlice(variables,
                    timeIndex);

                Assertions
                    .assertThat(testTimeSliceArrayList.size())
                    .isEqualTo(1);
                final Double[] testTimeSliceArray = testTimeSliceArrayList.get(0);
                Assertions
                    .assertThat(testTimeSliceArray.length)
                    .isEqualTo(1 * 180 * 600);

                // Compare to the control.
                this.compareArrays(controlSection, testTimeSliceArray);

            }

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Attempt to read a slice larger than that supported by the variable. This test will use the
     * variable <code>wspeed_u</code>, which has three (3) dimensions: 2 (time) x 180 x 600.
     */
    @Test
    public void testReadUtilsBaseShape() {
        try {
            NetcdfDataset dataset = NetcdfDataset.openDataset(ReadUtilsTest.TEST_FILENAME);
            Variable variable = dataset.findVariable("wspeed_u");
            List<Variable> variables = new ArrayList<>();
            variables.add(variable);

            // Do a valid read.
            int[] actualShape = new int[]{2, 180, 600};
            int[] noOffset = new int[]{0, 0, 0};
            Array allData = ReadUtils.readData(variable, actualShape, noOffset);
            Assertions
                .assertThat(allData.getSize())
                .isEqualTo(2 * 180 * 600);

            // Do an invalid read.
            try {
                int[] invalidShape = new int[]{5, 180, 600};
                ReadUtils.readData(variable, invalidShape, noOffset);
                Assertions.fail("Bad read passed unexpectedly.");
            } catch (Exception e) {
                Assertions
                    .assertThat(e.getCause())
                    .isInstanceOf(ucar.ma2.InvalidRangeException.class);
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Instantiate the {@link ReadUtils} class for 100% code coverage.
     */
    @Test
    public void testInstantiateReadUtils() {
        try {
            new ReadUtils();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

}
