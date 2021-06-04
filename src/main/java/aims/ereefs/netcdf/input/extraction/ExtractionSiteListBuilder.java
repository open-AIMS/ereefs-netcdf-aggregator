package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.regrid.Coordinate;
import aims.ereefs.netcdf.regrid.IndexWithDistance;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class that builds a list of {@link ExtractionSite} objects based on a list of specified
 * {@code Sites}. To construct a single {@link ExtractionSite}, this class starts from the
 * corresponding {@code site}, and increases the size of a search box by
 * {@link #BOX_WIDTH_INCREASE_INCREMENT} steps until it finds {@link #DEFAULT_MINIMUM_NEIGHBOURS}
 * nearest neighbours, or it reaches {@link #MAX_BOX_WIDTH_INCREASES} and stops.
 *
 * @author Aaron Smith
 */
public class ExtractionSiteListBuilder {

    static protected Logger logger = LoggerFactory.getLogger(ExtractionSiteListBuilder.class);

    static final protected String SITE_NAME_PROPERTY = "name";
    static final protected String LATITUDE_PROPERTY = "lat";
    static final protected String LONGITUDE_PROPERTY = "lon";

    /**
     * The default number of neighbours to find.
     */
    static final protected int DEFAULT_MINIMUM_NEIGHBOURS = 6;

    /**
     * The size the box increases each loop to find closest neighbours.
     */
    static final protected double BOX_WIDTH_INCREASE_INCREMENT = 0.03;

    /**
     * The maximum number of times the box width will be increased by
     * {@link #BOX_WIDTH_INCREASE_INCREMENT}.
     */
    static final protected int MAX_BOX_WIDTH_INCREASES = 6;

    /**
     * Convenience method when searching for closest neighbours. This method invokes
     * {@link #build(JsonNode, Map, int)} with {@link #DEFAULT_MINIMUM_NEIGHBOURS}.
     */
    static public List<ExtractionSite> build(JsonNode rootNode,
                                             Map<Integer, Coordinate> indexToCoordinateMap) {
        return ExtractionSiteListBuilder.build(
            rootNode,
            indexToCoordinateMap,
            DEFAULT_MINIMUM_NEIGHBOURS
        );
    }

    static public List<ExtractionSite> build(JsonNode rootNode,
                                             Map<Integer, Coordinate> indexToCoordinateMap,
                                             int minimumNeighbours) {

        // Build a navigable set of longitude values.
        final NavigableMap<Double, List<Integer>> latToIndexesMap = new TreeMap<>();
        final NavigableMap<Double, List<Integer>> lonToIndexesMap = new TreeMap<>();
        int latNanCount = 0;
        int lonNanCount = 0;
        for (int index : indexToCoordinateMap.keySet()) {
            final Coordinate coordinate = indexToCoordinateMap.get(index);
            final Double latitude = coordinate.getLatitude();
            if (Double.isNaN(latitude)) {
                latNanCount++;
            } else {
                if (!latToIndexesMap.containsKey(latitude)) {
                    latToIndexesMap.put(
                        latitude,
                        new ArrayList<Integer>() {{
                            add(index);
                        }}
                    );
                } else {
                    latToIndexesMap.get(latitude).add(index);
                }
            }
            final Double longitude = coordinate.getLongitude();
            if (Double.isNaN(longitude)) {
                lonNanCount++;
            } else {
                if (!lonToIndexesMap.containsKey(longitude)) {
                    lonToIndexesMap.put(
                        longitude,
                        new ArrayList<Integer>() {{
                            add(index);
                        }}
                    );
                } else {
                    lonToIndexesMap.get(longitude).add(index);
                }
            }
        }
        logger.debug("NaNs in latitude array: " + latNanCount);
        logger.debug("NaNs in longitude array: " + lonNanCount);

        // Determine the bounds of the dataset.
        double latBound1 = latToIndexesMap.firstKey();
        double latBound2 = latToIndexesMap.lastKey();
        double lonBound1 = lonToIndexesMap.firstKey();
        double lonBound2 = lonToIndexesMap.lastKey();
        logger.debug("latBound1: " + latBound1);
        logger.debug("latBound2: " + latBound2);
        logger.debug("lonBound1: " + lonBound1);
        logger.debug("lonBound2: " + lonBound2);

        // Build the list of extraction sites.
        final List<ExtractionSite> extractionSites = new ArrayList<>();
        int siteIndex = 0;
        while (siteIndex < rootNode.size()) {
            final JsonNode siteNode = rootNode.get(siteIndex);
            final String siteName = siteNode.get(SITE_NAME_PROPERTY).asText();
            final double latitude = siteNode.get(LATITUDE_PROPERTY).asDouble();
            final double longitude = siteNode.get(LONGITUDE_PROPERTY).asDouble();

            if (
                (latitude < latBound1 && latitude < latBound2) ||
                    (latitude > latBound1 && latitude > latBound2) ||
                    (longitude < lonBound1 && longitude < lonBound2) ||
                    (longitude > lonBound1 && longitude > lonBound2)
            ) {
                logger.debug(siteName + " is outside dataset range.");
            }

            // Find at least four (4) other coordinates nearby. To do this, expand a row/column
            // around the site until enough coordinates fit in both the row and column.
            List<Integer> indexesWithinBox = new ArrayList<Integer>();

            // Define the size of the box.
            double boxWidth = 0.0;

            while (indexesWithinBox.size() < minimumNeighbours &&
                boxWidth < BOX_WIDTH_INCREASE_INCREMENT * MAX_BOX_WIDTH_INCREASES) {
                boxWidth += BOX_WIDTH_INCREASE_INCREMENT;
                final Collection<List<Integer>> matchByLatitude = latToIndexesMap.subMap(
                    latitude - boxWidth,
                    latitude + boxWidth
                ).values();
                final Collection<List<Integer>> matchByLongitude = lonToIndexesMap.subMap(
                    longitude - boxWidth,
                    longitude + boxWidth
                ).values();

                // Simplify the structure.
                Set<Integer> matchByLatitudeFlat = flatten(matchByLatitude);
                Set<Integer> matchByLongitudeFlat = flatten(matchByLongitude);

                // At this point, we have a list of indexes which match latitude, and another list of
                // indexes that match longitude. Match the lists to find only those indexes in both lists.
                indexesWithinBox.clear();
                matchByLatitudeFlat.retainAll(matchByLongitudeFlat);
                indexesWithinBox.addAll(matchByLatitudeFlat);
            }

            List<IndexWithDistance> neighbours = new ArrayList<>();
            for (int index : indexesWithinBox) {
                Coordinate neighbour = indexToCoordinateMap.get(index);

                double distance = Math.sqrt(Math.pow((latitude - neighbour.getLatitude()), 2) +
                    Math.pow((longitude - neighbour.getLongitude()), 2));
                neighbours.add(new IndexWithDistance(index, distance));
            }


            // Find the four closest cells and build the ExtractionSite.
            Collections.sort(neighbours, IndexWithDistance::compareTo);
            extractionSites.add(
                new ExtractionSite(
                    Integer.toString(siteIndex),
                    siteName,
                    latitude,
                    longitude,
                    neighbours.subList(
                        0,
                        (neighbours.size() <= minimumNeighbours ? neighbours.size() : minimumNeighbours)
                    )
                )
            );

            siteIndex++;

        }

        return extractionSites;
    }

    private static Set<Integer> flatten(Collection<List<Integer>> in) {
        Set<Integer> out = new HashSet<>();
        for (List<Integer> l : in) {
            out.addAll(l);
        }
        return out;
    }
}
