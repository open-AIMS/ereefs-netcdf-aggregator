package aims.ereefs.netcdf.grid.regular;

import aims.ereefs.netcdf.regrid.CacheReader;
import aims.ereefs.netcdf.regrid.CacheWriter;
import aims.ereefs.netcdf.regrid.RegularGridMapper;
import aims.ereefs.netcdf.regrid.RegularGridMapperBuilder;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

/**
 * Tests for the {@link CacheWriter} and {@link CacheReader} classes.
 *
 * @author Aaron Smith
 */
public class CacheTest {

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

        // Cache it.
        final String filename = System.getProperty("java.io.tmpdir") + File.separator +
            UUID.randomUUID().toString() + ".ser";
        CacheWriter.write(regularGridMapper, filename);

        // Uncache it.
        final RegularGridMapper regularGridMapper2 = CacheReader.readAsRegularGridMapper(filename);

        // Validate the result.
        RegularGridMapperTestUtils.validateRegularGridMapper(regularGridMapper2);
    }
}
