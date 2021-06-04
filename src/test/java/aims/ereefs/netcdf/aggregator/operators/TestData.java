package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.util.DateUtils;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Data for use with testing math-based operators.
 *
 * @author Aaron Smith
 */
public class TestData {

    /**
     * Valid test data which consists of multiple time slices for a 2 x 2 grid with a height of 2.
     */
    final static public Map<Double, Double[]> TEST_DATA = new TreeMap<Double, Double[]>() {
        {
            double startDateTime = NetcdfDateUtils.fromLocalDateTime(
                DateUtils.getDateUnit(), LocalDateTime.of(2016, 9, 29, 0, 0));

            // Day 1.
            put(
                startDateTime + 0.0,
                new Double[]{
                    Double.NaN, 5.0,   // z = 1
                    0.0, null,

                    null, 10.0, // z = 2
                    2.2, 7.8
                }
            );

            // Day 2.
            put(
                startDateTime + 1.0,
                new Double[]{
                    Double.NaN, null,  // z = 1
                    2.2, 0.0,

                    0.0, 5.0,   // z = 2
                    null, 5.0
                }
            );

            // Day 3.
            put(
                startDateTime + 2.0,
                new Double[]{
                    Double.NaN, 0.0,  // z = 1
                    6.0, 3.3,

                    4.4, 5.0,   // z = 2
                    0.0, null
                }
            );

            // Day 4.
            put(
                startDateTime + 3.0,
                new Double[]{
                    Double.NaN, 4.4,   // z = 1
                    null, 5.0,

                    4.4, null,  // z = 2
                    1.1, 6.0
                }
            );
        }
    };

}
