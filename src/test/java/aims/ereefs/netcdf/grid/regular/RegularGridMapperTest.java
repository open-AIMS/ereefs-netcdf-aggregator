package aims.ereefs.netcdf.grid.regular;

import aims.ereefs.netcdf.regrid.RegularGridMapper;
import aims.ereefs.netcdf.regrid.RegularGridMapperBuilder;
import org.junit.Test;
import ucar.ma2.Array;

/**
 * Tests for the {@link RegularGridMapper} class.
 *
 * @author Aaron Smith
 */
public class RegularGridMapperTest {

    /**
     * Test {@link RegularGridMapper#curvedToRegular(Array)} on a 4D array
     * (time: 1, depth: 3, x: 5, y:5).
     */
    @Test
    public void testValid() {

        // Instantiate a RegularGridMapper.
        double resolution = 0.5;
        RegularGridMapper regularGridMapper = RegularGridMapperBuilder.make(
            CurvilinearTestData.latitudeArray,
            CurvilinearTestData.longitudeArray,
            resolution,
            null
        );

        // Validate the result.
        RegularGridMapperTestUtils.validateRegularGridMapper(regularGridMapper);
    }
}
