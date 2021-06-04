package aims.ereefs.netcdf.output.summary;

import aims.ereefs.netcdf.regrid.Coordinate;
import aims.ereefs.netcdf.regrid.IndexWithDistance;
import aims.ereefs.netcdf.input.extraction.CoordinateMapBuilder;
import aims.ereefs.netcdf.input.extraction.ExtractionSite;
import aims.ereefs.netcdf.input.extraction.ExtractionSiteListBuilder;
import aims.ereefs.netcdf.input.extraction.TestData;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link SiteBasedSummaryAccumulatorImpl} class.
 *
 * @author Aaron Smith
 */
public class SiteBasedSummaryAccumulatorImplTest {

    @Test
    public void testSiteAccumulation() {

        // Create 2 depths.
        List<Double> selectedDepths = new ArrayList<Double>() {{
            add(-1.5);
            add(-17.75);
        }};

        // Build the coordinate map.
        final Map<Integer, Coordinate> indexToCoordinateMap = CoordinateMapBuilder.build(
            TestData.latitudeArray, TestData.longitudeArray, false);

        // Build the list of extraction sites.
        final List<ExtractionSite> extractionSiteList =
            ExtractionSiteListBuilder.build(
                TestData.EXTRACT_SITES_JSON,
                indexToCoordinateMap
            );

        // Instantiate the accumulator.
        final int layerSize = TestData.LATITUDE_COUNT * TestData.LONGITUDE_COUNT;
        SummaryAccumulator accumulator = new SiteBasedSummaryAccumulatorImpl(
            selectedDepths,
            layerSize,
            extractionSiteList
        );

        // Create the test data.
        Double[] testData = new Double[
            selectedDepths.size() * TestData.LATITUDE_COUNT * TestData.LONGITUDE_COUNT
            ];
        int indexCount = 0;
        for (double depth : selectedDepths) {
            for (int latIndex = 0; latIndex < TestData.LATITUDE_COUNT; latIndex++) {
                for (int lonIndex = 0; lonIndex < TestData.LONGITUDE_COUNT; lonIndex++) {
                    testData[indexCount] = depth + indexCount;
                    indexCount++;
                }
            }
        }

        // Accumulate the data and retrieve the result.
        accumulator.add(
            new ArrayList<Double[]>() {{
                add(testData);
            }}
        );
        Map<Double, Map<String, List<Double>>> depthToAccumulationBucketsMap =
            accumulator.getDepthToAccumulationBucketsMap();
        Assertions
            .assertThat(depthToAccumulationBucketsMap.keySet().size())
            .isEqualTo(2);

        for (int depthIndex = 0; depthIndex < selectedDepths.size(); depthIndex++) {
            double depth = selectedDepths.get(depthIndex);
            Map<String, List<Double>> siteToAccumulationBucketMap = depthToAccumulationBucketsMap.get(depth);
            Assertions
                .assertThat(siteToAccumulationBucketMap.keySet().size())
                .isEqualTo(3);

            // Process each site separately.
            for (ExtractionSite extractionSite : extractionSiteList) {
                List<Double> accumulationBucket = siteToAccumulationBucketMap.get(extractionSite.getId());

                // Sites without neighbours will have no accumulated data.
                if (extractionSite.getNeighbours().isEmpty()) {
                    Assertions
                        .assertThat(accumulationBucket)
                        .hasSize(0);
                } else {

                    // Calculate the expected result for the site and compare to the returned value.
                    Assertions
                        .assertThat(accumulationBucket)
                        .hasSize(1);
                    double sum = 0.0;
                    double sumWeights = 0.0;
                    for (IndexWithDistance neighbour : extractionSite.getNeighbours()) {
                        double value = testData[(depthIndex * layerSize) + neighbour.getIndex()];
                        if (!Double.isNaN(value)) {
                            sum += value * neighbour.getWeight();
                            sumWeights += neighbour.getWeight();
                        }
                    }
                    Assertions
                        .assertThat(accumulationBucket.get(0))
                        .isEqualTo(sum / sumWeights);
                }
            }

        }

    }

}
