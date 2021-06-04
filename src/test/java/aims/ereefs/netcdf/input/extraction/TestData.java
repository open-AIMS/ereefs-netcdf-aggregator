package aims.ereefs.netcdf.input.extraction;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index2D;

/**
 * Helper class for building/manipulating test data for Data Extraction related tests. These tests
 * are mostly found in this package, but may be in other packages as a result of the code base.
 *
 * @author Aaron Smith
 */
public class TestData {

    /*
        Grid layout:

            0.0    0.05    0.10    0.15    0.20

            0.05

            0.10
     */
    static final public int LONGITUDE_COUNT = 5;
    static final public double LONGITUDE_START = 0.0;
    static final public int LATITUDE_COUNT = 3;
    static final public double LATITUDE_START = 0.0;
    static final public double RESOLUTION = 0.05;
    static public Array longitudeArray;
    static public Array latitudeArray;

    // Extraction site definition.
    static final public ArrayNode EXTRACT_SITES_JSON;

    static {

        final int[] shape = new int[]{LATITUDE_COUNT, LONGITUDE_COUNT};
        longitudeArray = Array.factory(DataType.DOUBLE, shape);
        latitudeArray = Array.factory(DataType.DOUBLE, shape);
        Index index = new Index2D(shape);

        // Y-axis
        for (int latitudeIndex = 0; latitudeIndex < LATITUDE_COUNT; latitudeIndex++) {

            // X-axis
            for (int longitudeIndex = 0; longitudeIndex < LONGITUDE_COUNT; longitudeIndex++) {
                latitudeArray.setDouble(
                    index.set(latitudeIndex, longitudeIndex),
                    LATITUDE_START + latitudeIndex * RESOLUTION
                );
                longitudeArray.setDouble(
                    index.set(latitudeIndex, longitudeIndex),
                    LONGITUDE_START + longitudeIndex * RESOLUTION
                );
            }
        }

        EXTRACT_SITES_JSON = new ArrayNode(new JsonNodeFactory(false));
        // If we consider 0,0 to be top left for description purposes. This is our first site and
        // should have nearest neighbours are itself, one to the right, one diagonal bottom right,
        // and one below.
        EXTRACT_SITES_JSON
            .addObject()
            .put(ExtractionSiteListBuilder.SITE_NAME_PROPERTY, "Site1")
            .put(ExtractionSiteListBuilder.LONGITUDE_PROPERTY, LONGITUDE_START)
            .put(ExtractionSiteListBuilder.LATITUDE_PROPERTY, LATITUDE_START);

        // This site is in the middle of the grid, but does not match a specific cell.
        EXTRACT_SITES_JSON
            .addObject()
            .put(ExtractionSiteListBuilder.SITE_NAME_PROPERTY, "Site2")
            .put(ExtractionSiteListBuilder.LONGITUDE_PROPERTY,
                LONGITUDE_START + 2 * RESOLUTION - RESOLUTION / 3)
            .put(ExtractionSiteListBuilder.LATITUDE_PROPERTY,
                LATITUDE_START + 2 * RESOLUTION - RESOLUTION / 3);

        // This site is well outside of the grid.
        EXTRACT_SITES_JSON
            .addObject()
            .put(ExtractionSiteListBuilder.SITE_NAME_PROPERTY, "Site3")
            .put(ExtractionSiteListBuilder.LONGITUDE_PROPERTY,
                LONGITUDE_START + (LONGITUDE_COUNT * 2) * RESOLUTION)
            .put(ExtractionSiteListBuilder.LATITUDE_PROPERTY,
                LATITUDE_START + (LATITUDE_COUNT * 2) * RESOLUTION);

    }

}
