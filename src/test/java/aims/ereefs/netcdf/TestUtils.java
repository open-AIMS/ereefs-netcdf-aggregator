package aims.ereefs.netcdf;

import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Common utilities and constants used in tests.
 *
 * @author Aaron Smith
 */


public class TestUtils {

    /**
     * Location of test NetCDF file(s).
     */
    static final public String INPUT_DIRECTORY = "testdata/input/";

    /**
     * Path and filename of a test file.
     */
    static final public String TEST_FILENAME = "small.nc";

    /**
     * Suitable destination location for tests.
     */
    static final public String OUTPUT_DIRECTORY = "testdata/output";

    /**
     * Utility method to round a {@code Double} to {@code 2} decimal places to remove floating
     * point calculation errors.
     */
    static public Double round(Double value) {
        return Math.round(value * 100) / 100.0;
    }

    /**
     * Utility for comparing two {@code Double} arrays using {@link #round(Double)} method for each
     * value.
     */
    static public void assertSame(Double[] actual,
                                  Double[] expected) {
        Assertions
            .assertThat(actual)
            .hasSameSizeAs(expected);
        for (int index = 0; index < actual.length; index++) {

            // Remove precision from answers to avoid floating point errors and compare.
            Double actualValue = actual[index];
            Double expectedValue = expected[index];
            if ((actualValue != null) && !Double.isNaN(actualValue)) {
                Assertions
                    .assertThat(TestUtils.round(actualValue))
                    .as("index: %s", index)
                    .isEqualTo(TestUtils.round(expectedValue));
            } else {
                Assertions
                    .assertThat(expectedValue)
                    .as("index: %s", index)
                    .isNaN();
            }
        }
    }

    /**
     * Utility for comparing two {@code Lists}, each containing one or more {@code Double} arrays
     * by using the {@link #assertSame(Double[], Double[])} method.
     */
    static public void assertSame(List<Double[]> actualResults,
                                  List<Double[]> expectedResults) {
        Assertions
            .assertThat(actualResults)
            .hasSameSizeAs(expectedResults);
        for (int arrayIndex = 0; arrayIndex < actualResults.size(); arrayIndex++) {
            TestUtils.assertSame(actualResults.get(arrayIndex), expectedResults.get(arrayIndex));
        }
    }


}
