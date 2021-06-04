package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.regrid.Coordinate;
import aims.ereefs.netcdf.regrid.IndexWithDistance;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link ExtractionSiteListBuilder} class.
 *
 * @author Aaron Smith
 */
public class ExtractionSiteListBuilderTest {

    @Test
    public void testNearestNeighbours() {

        // Build the coordinate map.
        final Map<Integer, Coordinate> indexToCoordinateMap = CoordinateMapBuilder.build(
            TestData.latitudeArray, TestData.longitudeArray, false);

        // Build the list of extraction sites.
        final List<ExtractionSite> extractionSiteList =
            ExtractionSiteListBuilder.build(
                TestData.EXTRACT_SITES_JSON,
                indexToCoordinateMap,
                4
            );

        Assertions
            .assertThat(extractionSiteList)
            .hasSize(3);

        ExtractionSite extractionSite = extractionSiteList.get(0);
        List<IndexWithDistance> neighbours = extractionSite.getNeighbours();
        Assertions.assertThat(neighbours).hasSize(4);
        Assertions.assertThat(neighbours.get(0).getIndex()).isEqualTo(0);
        Assertions.assertThat(neighbours.get(1).getIndex()).isEqualTo(1);
        Assertions.assertThat(neighbours.get(2).getIndex()).isEqualTo(5);
        Assertions.assertThat(neighbours.get(3).getIndex()).isEqualTo(6);

        extractionSite = extractionSiteList.get(1);
        neighbours = extractionSite.getNeighbours();
        Assertions.assertThat(neighbours).hasSize(4);
        Assertions.assertThat(neighbours.get(0).getIndex()).isEqualTo(12);
        Assertions.assertThat(neighbours.get(1).getIndex()).isEqualTo(7);
        Assertions.assertThat(neighbours.get(2).getIndex()).isEqualTo(11);
        Assertions.assertThat(neighbours.get(3).getIndex()).isEqualTo(6);

        extractionSite = extractionSiteList.get(2);
        neighbours = extractionSite.getNeighbours();
        Assertions.assertThat(neighbours).hasSize(0);
    }

}
