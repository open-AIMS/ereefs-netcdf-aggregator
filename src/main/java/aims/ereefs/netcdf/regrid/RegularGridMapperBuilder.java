package aims.ereefs.netcdf.regrid;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import java.util.*;

/**
 * Builder class to instantiate and populate a {@link RegularGridMapper}.
 *
 * @author Aaron Smith
 */
public class RegularGridMapperBuilder {

    private static Logger logger = LoggerFactory.getLogger(RegularGridMapperBuilder.class);

    /**
     * Constant determining the number of decimal places to use for precision when converting from
     * the original dimensions to longitude/latitude.
     */
    private static final int NUMBER_OF_DECIMAL_PLACES = 6;

    /**
     * Instantiate and populate the {@link RegularGridMapper} instance. This involves building a
     * regular grid and mapping each spot to a lon/lat coordinate ({@code regularGridToCoordinate}),
     */
    static public RegularGridMapper make(Array latitudeArray, Array longitudeArray,
                                         double resolution, String cacheLocation) {

        // Wrap the latitude and longitude in an ArrayWrapper for convenience.
        ArrayWrapper latitudeWrapper = new ArrayWrapper(latitudeArray);
        ArrayWrapper longitudeWrapper = new ArrayWrapper(longitudeArray);

        // Identify the size of the curved linear grid.
        final double firstLatitude = latitudeWrapper.getMinimumValue();
        final double lastLatitude = latitudeWrapper.getMaximumValue();
        final double firstLongitude = longitudeWrapper.getMinimumValue();
        final double lastLongitude = longitudeWrapper.getMaximumValue();

        // Define a map that binds an x,y point on the regular grid to an actual lat/lon coordinate.
        final Map<Point, Coordinate> regularGridToCoordinate = new HashMap<Point, Coordinate>();

        // We know the range of latitude and longitude values in the curvilinear grid, and
        // the resolution for the regular grid, calculate the number of steps in the latitude and
        // longitude. These factors (first, last, count) are used to populate the 'Point' part
        // of the 'regularGridToCoordinate' variable.
        final int latitudeCount = (int) Math.ceil((lastLatitude - firstLatitude) / resolution);
        final int longitudeCount = (int) Math.ceil((lastLongitude - firstLongitude) / resolution);

        // Build a reference cache to the latitude/longitude arrays to use after re-gridding.
        final Array outputLatitudeArray = new ArrayDouble.D1(latitudeCount);
        final Array outputLongitudeArray = new ArrayDouble.D1(longitudeCount);

        // Loop through each index in the latitude dimension of the regular grid, caching the index
        // and the corresponding latitude value.
        for (int latitudeIndex = 0; latitudeIndex < latitudeCount; latitudeIndex++) {
            final double latitude = RegularGridMapperBuilder.convertIndexToValue(resolution,
                NUMBER_OF_DECIMAL_PLACES, firstLatitude, latitudeIndex);
            outputLatitudeArray.setDouble(latitudeIndex, latitude);
        }

        // Loop through each index in the longitude dimension of the regular grid, caching the
        // index and the corresponding longitude value.
        for (int longitudeIndex = 0; longitudeIndex < longitudeCount; longitudeIndex++) {
            final double longitude = RegularGridMapperBuilder.convertIndexToValue(resolution,
                NUMBER_OF_DECIMAL_PLACES, firstLongitude, longitudeIndex);
            outputLongitudeArray.setDouble(longitudeIndex, longitude);
        }

        // Build a map between the index (Point) and the corresponding lat/lon values (Coordinate).
        for (int latitudeIndex = 0; latitudeIndex < latitudeCount; latitudeIndex++) {
            final double latitude = outputLatitudeArray.getDouble(latitudeIndex);
            for (int longitudeIndex = 0; longitudeIndex < longitudeCount; longitudeIndex++) {
                final double longitude = outputLongitudeArray.getDouble(longitudeIndex);
                final Point point = new Point(latitudeIndex, longitudeIndex);
                final Coordinate coordinate = new Coordinate(latitude, longitude);
                regularGridToCoordinate.put(point, coordinate);
            }
        }

        // Define a map that binds a point on the regular grid to a grouping of the closest
        // corresponding points on the curvilinear grid.
        final Map<Point, IndexWithDistance[]> regularGridToCurvedIndex = new HashMap<>();

        // If a cache file is specified and it exists, attempt to load the mapping.
        boolean isLoaded = false;
        if (cacheLocation != null) {
            Map<Point, IndexWithDistance[]> map = CacheReader.read(cacheLocation);
            if (map != null) {
                // Cache loaded, so set flag and dump temp map into actual map object.
                isLoaded = true;
                regularGridToCurvedIndex.putAll(map);
            }
        }

        // If a cache file has not been loaded, generate the mappings.
        if (!isLoaded) {
            logger.debug("Generating regular grid mappings.");
            RegularGridMapperBuilder.findClosestFromCurved(regularGridToCoordinate,
                regularGridToCurvedIndex, resolution, latitudeWrapper, longitudeWrapper);
        }

        // If a cache file is specified but loading had failed, attempt to serialise the mappings.
        if (cacheLocation != null && !isLoaded) {
            CacheWriter.write(regularGridToCurvedIndex, cacheLocation);
        }

        // Instantiate the RegularGridMapper.
        return new RegularGridMapper(
            latitudeCount,
            longitudeCount,
            outputLatitudeArray,
            outputLongitudeArray,
            regularGridToCurvedIndex
        );


    }

    /**
     * Helper method to convert an index to an actual latitude or longitude value.
     */
    private static double convertIndexToValue(double resolution, int numberOfDecimalPlaces, double firstValue, int index) {
        return Precision.round(firstValue + (resolution * index), numberOfDecimalPlaces);
    }

    /**
     * Populate the specified {@code regularGridToCurvedIndex} mapper. This is done by building a
     * collection of points on a regular/rectilinear grid (lat/lon) populates
     * regularGridToCurvedIndex for each point in the regular grid, finds the four closest points
     * in the curved grid.
     */
    private static void findClosestFromCurved(Map<Point, Coordinate> regularGridToCoordinate,
                                              Map<Point, IndexWithDistance[]> regularGridToCurvedIndex,
                                              double resolution,
                                              ArrayWrapper latitudeWrapper,
                                              ArrayWrapper longitudeWrapper) {
        int count = 0;
        int ignored = 0;
        int size = regularGridToCoordinate.size();
        logger.info("total points = " + size);

        // Loop through every cell of the output regular grid.
        for (Map.Entry<Point, Coordinate> entry : regularGridToCoordinate.entrySet()) {
            Point point = entry.getKey();
            Coordinate coordinate = entry.getValue();
            if (count % 10000 == 0) {
                logger.info("Count " + count + " of " + size + "; ignored: " + ignored);
            }

            // Find any curved linear pixels/cells that fall within the regular grid cell. We start
            // with a small box and continue to expand the box until we find at least four (4)
            // curved linear cells, or until the box gets unreasonably large.
            double boxWidth = 0.0;
            Collection<Integer> foundCurvedLinearPixels = new ArrayList<>();
            while (foundCurvedLinearPixels.size() < 4 && boxWidth < resolution * 3) {
                boxWidth += (resolution / 2);
                foundCurvedLinearPixels =
                    RegularGridMapperBuilder.findWithinBox(latitudeWrapper, longitudeWrapper,
                        coordinate, boxWidth);
            }

            // If no curved linear pixels were found, assume we are outside the bounds (eg: on land
            // or something). Otherwise, identify the four (4) closest curved linear pixels along
            // with their distances to the regular grid cell, so we can use a weighted mean when
            // converting from curved linear to regular grid later.
            if (!foundCurvedLinearPixels.isEmpty()) {
                List<IndexWithDistance> selectedInputsWithDistance =
                    RegularGridMapperBuilder.findClosestFour(latitudeWrapper, longitudeWrapper,
                        foundCurvedLinearPixels, coordinate, resolution);
                IndexWithDistance[] list = new IndexWithDistance[selectedInputsWithDistance.size()];
                for (int index = 0; index < selectedInputsWithDistance.size(); index++) {
                    list[index] = selectedInputsWithDistance.get(index);
                }
                regularGridToCurvedIndex.put(point, list);
            } else {
                ignored++;
            }
            count++;
        }
    }

    /**
     * Helper method to find a list of indexes from the input lat/lon arrays that fit within the
     * bounding box specified for the regular grid cell. Note that while the output
     * latitude/longitude arrays contain only unique values to the corresponding dimension, the
     * input latitude/longitude arrays contain an entry for every cell on the curved linear grid.
     * <p>
     * For example, consider these points (lat/lon):
     *
     * <ul>
     * <li>10,20</li>
     * <li>11,20</li>
     * <li>10,21</li>
     * <li>11,21</li>
     * </ul>
     * <p>
     * For regular grid, latitude array would contain {@code 10} and {@code 11}, while longitude
     * array would contain {@code 20} and {@code 21}.
     * <p>
     * For curved linear grid, latitude array would contain:
     *
     * <ul>
     * <li>{@code 10}</li>
     * <li>{@code 11}</li>
     * <li>{@code 10}</li>
     * <li>{@code 11}</li>
     * </ul>
     * <p>
     * and longitude array would contain:
     *
     * <ul>
     * <li>{@code 20}</li>
     * <li>{@code 20}</li>
     * <li>{@code 21}</li>
     * <li>{@code 21}</li>
     * </ul>
     * <p>
     * That way, for the curved linear grid, an index of 0 matches {@code 10/20}.
     *
     * @param coordinate the lat/lon of the regular grid cell.
     * @param boxWidth   the size of the bounding box in which to search.
     * @return The indexes from the curved grid that fall within the bounding box on the regular
     * grid.
     */
    private static Collection<Integer> findWithinBox(ArrayWrapper latitudeWrapper,
                                                     ArrayWrapper longitudeWrapper,
                                                     Coordinate coordinate,
                                                     double boxWidth) {

        // Identify all
        final Collection<List<Integer>> matchByLatitude =
            latitudeWrapper.getValueToIndex().subMap(
                coordinate.getLatitude() - boxWidth,
                coordinate.getLatitude() + boxWidth
            ).values();
        final Collection<List<Integer>> matchByLongitude =
            longitudeWrapper.getValueToIndex().subMap(
                coordinate.getLongitude() - boxWidth,
                coordinate.getLongitude() + boxWidth
            ).values();

        // Simplify the structure.
        Set<Integer> matchByLatitudeFlat = RegularGridMapperBuilder.flatten(matchByLatitude);
        Set<Integer> matchByLongitudeFlat = RegularGridMapperBuilder.flatten(matchByLongitude);

        // At this point, we have a list of indexes which match latitude, and another list of
        // indexes that match longitude. Match the lists to find only those indexes in both lists.
        matchByLatitudeFlat.retainAll(matchByLongitudeFlat);
        return matchByLatitudeFlat;
    }

    /**
     * Helper method to flatten a collection of lists of integers into a single list of integers.
     */
    private static Set<Integer> flatten(Collection<List<Integer>> in) {
        Set<Integer> out = new HashSet<>();
        for (List<Integer> l : in) {
            out.addAll(l);
        }
        return out;
    }


    /**
     * Calculate the distances from each of the curved linear pixels to the
     * <code>coordinate</code> that represents a cell on the regular grid. Return only the four (4)
     * closest pixels.
     *
     * @param foundCurvedLinearPixels these are indexes to the curved grid
     * @param coordinate              cell on the regular grid.
     * @return the four closest pixels and the corresponding distance
     */
    private static List<IndexWithDistance> findClosestFour(ArrayWrapper latitudeWrapper,
                                                           ArrayWrapper longitudeWrapper,
                                                           Collection<Integer> foundCurvedLinearPixels,
                                                           Coordinate coordinate,
                                                           double resolution) {
        List<IndexWithDistance> l = new ArrayList<>(foundCurvedLinearPixels.size());
        for (Integer index : foundCurvedLinearPixels) {
            double distance = RegularGridMapperBuilder.calculateDistance(latitudeWrapper,
                longitudeWrapper, coordinate, index, resolution);
            l.add(new IndexWithDistance(index, distance));
        }

        // Perform a natural sort. Note that IndexWithDistance implements 'compareTo' to sort on
        // the 'distance' property.
        Collections.sort(l);

        // Return only the top (closest) four pixels to the coordinate.
        if (l.size() > 4) {
            return new ArrayList<>(l.subList(0, 4));
        } else {
            return l;
        }
    }

    /**
     * Calculate the distance between the point on the curved linear grid identified by 'index' and
     * the cell on the regular grid identified by 'coordinate'.
     *
     * @param coordinate the cell on the regular grid.
     * @param index      the index to a point on the curved grid.
     * @return euclidean distance from the coordinate to the point on the curved grid
     */
    private static double calculateDistance(ArrayWrapper latitudeWrapper,
                                            ArrayWrapper longitudeWrapper,
                                            Coordinate coordinate,
                                            Integer index,
                                            double resolution) {
        final Double latitude = latitudeWrapper.getIndexToValue().get(index);
        final Double longitude = longitudeWrapper.getIndexToValue().get(index);
        final double distance = Math.sqrt(Math.pow((coordinate.getLatitude() - latitude), 2) +
            Math.pow((coordinate.getLongitude() - longitude), 2));

        if (distance > (resolution * 5)) {
            logger.warn("Distance" + distance + "too great. " + coordinate.toString() + " " + latitude + " " + longitude);
        }
        return distance;

    }


}
