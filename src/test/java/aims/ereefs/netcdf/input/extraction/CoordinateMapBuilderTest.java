package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.regrid.Coordinate;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link CoordinateMapBuilder} class.
 *
 * @author Aaron Smith
 */
public class CoordinateMapBuilderTest {

    /**
     * Test building a {@code Map} of {@link Coordinate}s from a curvilinear grid. Note that the
     * grid itself is NOT curvilinear, but it is defined as a curvilinear grid would be.
     */
    @Test
    public void testBuildCurvilinear() {

        // Define the expected results.
        final List<Double[]> results = new ArrayList<Double[]>();
        for (int lonCount = 0; lonCount < TestData.LONGITUDE_COUNT; lonCount++) {
            for (int latCount = 0; latCount < TestData.LATITUDE_COUNT; latCount++) {
                results.add(
                    new Double[]{
                        (double) TestData.LONGITUDE_START + lonCount * TestData.RESOLUTION,
                        (double) TestData.LATITUDE_START + latCount * TestData.RESOLUTION
                    }
                );
            }
        }

        // Build the list.
        Map<Integer, Coordinate> indexToCoordinateMap = CoordinateMapBuilder.build(
            TestData.latitudeArray, TestData.longitudeArray, false);

        // Verify the results.
        Assertions
            .assertThat(indexToCoordinateMap)
            .hasSameSizeAs(results);
        for (int index : indexToCoordinateMap.keySet()) {
            final Coordinate coordinate = indexToCoordinateMap.get(index);
            final Iterator<Double[]> resultsIterator = results.iterator();
            while (resultsIterator.hasNext()) {
                final Double[] result = resultsIterator.next();
                if ((result[0] == coordinate.getLongitude()) && (result[1] == coordinate.getLatitude())) {
                    resultsIterator.remove();
                }
            }
        }
        Assertions.assertThat(results).isEmpty();

    }

}
