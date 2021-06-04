package aims.ereefs.netcdf.input.geojson;

import aims.ereefs.netcdf.input.netcdf.InputDataset;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code Builder} to map each cell (the index in the time slices) to a zone/region based on a
 * GeoJSON file. A {@code null} signifies that the point is outside of all zones/regions.
 *
 * @author Aaron Smith
 */
public class IndexToZoneIdMapBuilder {

    static protected Logger logger = LoggerFactory.getLogger(IndexToZoneIdMapBuilder.class);

    /**
     * Perform the function of the {@code Task}.
     */
    static public List<String> build(InputDataset referenceDataset,
                                     JSONObject zonesGeoJson,
                                     boolean isRectilinearGrid) throws RuntimeException {

        // The GEOJson file contains a list of zones, with each zone defined by a further list of
        // polygons. Build the map that binds the ZoneId to every polygon defined within that Zone.
        final Map<String, Path2D[]> zoneIdToPolygonsMap = buildZoneIdToPolygonsMap(zonesGeoJson);

        // Using the polygons, map each lat/lon from the reference dataset to a zone.
        ZoneLookUp zoneLookUp = new ZoneLookUp(zoneIdToPolygonsMap);

        // Obtain the references to the lat/lon data.
        logger.debug("Reading latitude variable.");
        Array latitudeArray = null;
        try {
            latitudeArray = referenceDataset.getLatitudeVariable().read();
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to read latitude data from reference dataset.",
                throwable);
        }
        logger.debug("Reading longitude variable.");
        Array longitudeArray = null;
        try {
            longitudeArray = referenceDataset.getLongitudeVariable().read();
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to read longitude data from reference dataset.",
                throwable);
        }

        // Define the output variable that will map the index of the point to the corresponding
        // zone/region. Since we are using a List, the order of the list becomes the 'index', which
        // makes it vital that the order of the list does not change.
        List<String> indexToZoneIdMap = new ArrayList<>();

        // Define some basic statistic variables.
        int noZoneCount = 0;
        int inZoneCount = 0;

        logger.debug("Map to lat/lon - start");
        // How we build the list depends on whether the grid is curvilinear or rectilinear.
        if (isRectilinearGrid) {

            logger.debug("Treating as a rectilinear grid.");

            long latSize = latitudeArray.getSize();
            logger.debug("latSize: " + latSize);
            long lonSize = longitudeArray.getSize();
            logger.debug("lonSize: " + lonSize);
            final long total = latSize * lonSize;
            logger.debug("total: " + total);
            int index = 0;
            for (int latIndex = 0; latIndex < latSize; latIndex++) {
                double latitude = latitudeArray.getDouble(latIndex);
                for (int lonIndex = 0; lonIndex < lonSize; lonIndex++) {
                    double longitude = longitudeArray.getDouble(lonIndex);

                    // Identify the zoneId for the lon/lat and add it to the output variable.
                    String zoneId = zoneLookUp.findZoneId(longitude, latitude);
                    indexToZoneIdMap.add(zoneId);

                    // Calculate the statistics.
                    if (zoneId == null) {
                        noZoneCount++;
                    } else {
                        inZoneCount++;
                    }

                    // Write to logs for every 10,000 points.
                    if (index % 10000 == 0) {
                        logger.debug(index + " out of " + total + "; inZone: " + inZoneCount +
                            "; noZone: " + noZoneCount);
                    }
                    index++;
                }
            }

        } else {

            logger.debug("Treating as a curvilinear grid.");

            // Loop through every point in the lat/lon arrays, identifying the zone the point belongs
            // to. The output is a List, but the order of the list identifying the order of the
            // data points, so it is vital that the order is preserved.
            long size = longitudeArray.getSize();
            for (int index = 0; index < size; index++) {

                // Identify the longitude/latitude for the specific index.
                double longitude = longitudeArray.getDouble(index);
                double latitude = latitudeArray.getDouble(index);

                // Identify the zoneId for the lon/lat and add it to the output variable.
                String zoneId = zoneLookUp.findZoneId(longitude, latitude);
                indexToZoneIdMap.add(zoneId);

                // Calculate the statistics.
                if (zoneId == null) {
                    noZoneCount++;
                } else {
                    inZoneCount++;
                }

                // Write to logs for every 10,000 points.
                if (index % 10000 == 0) {
                    logger.debug(index + " out of " + size + "; inZone: " + inZoneCount +
                        "; noZone: " + noZoneCount);
                }
            }

        }
        logger.debug("Map to lat/lon - end");
        logger.debug("indexToZoneIdMap.size: " + indexToZoneIdMap.size());

        return indexToZoneIdMap;

    }

    // ---------------------------------------------------------------------------------------------
    // Build a map from ZoneId to the polygons that form the zone. A single zone can consist of
    // multiple polygons. This assumes that every "feature" in the GeoJson is a distinct Zone.
    static protected Map<String, Path2D[]> buildZoneIdToPolygonsMap(JSONObject zonesGeoJson) {

        final Map<String, Path2D[]> zoneIdToPolygonsMap = new HashMap<>();

        // Loop through each feature. Assuming here that a feature equates to a Zone/Region.
        final JSONArray features = zonesGeoJson.getJSONArray("features");
        for (int featureindex = 0; featureindex < features.length(); featureindex++) {
            JSONObject feature = features.getJSONObject(featureindex);
            final String zoneId = String.valueOf(featureindex);

            // Retrieve the list of polygons for the feature.
            JSONArray polygonDefns = feature.getJSONObject("geometry").getJSONArray("coordinates");

            // ------------
            Path2D[] polygons = new Path2D[polygonDefns.length()];
            zoneIdToPolygonsMap.put(zoneId, polygons);
            // ------------

            // For each polygon, retrieve the list of coordinates that describe it.
            for (int polygonDefnIndex = 0; polygonDefnIndex < polygonDefns.length(); polygonDefnIndex++) {
                JSONArray coordArray = polygonDefns.getJSONArray(polygonDefnIndex).getJSONArray(0);
                Path2D polygon = null;
                for (int pointIndex = 0; pointIndex < coordArray.length(); pointIndex++) {
                    JSONArray pointJson = coordArray.getJSONArray(pointIndex);
                    double longitude = pointJson.getDouble(0);
                    double latitude = pointJson.getDouble(1);
                    if (polygon == null) {
                        polygon = new Path2D.Double();
                        polygon.moveTo(longitude, latitude);

                        // ------------
                        polygons[polygonDefnIndex] = polygon;
                        // ------------

                    } else {
                        polygon.lineTo(longitude, latitude);
                    }
                }
                polygon.closePath();
            }
        }

        return zoneIdToPolygonsMap;

    }

    /**
     * Internal helper class that looks up the zone/region based of a point defined by a lat/lon,
     * based on the processing of a GeoJSON file defining the zones/regions.
     */
    static class ZoneLookUp {

        protected String[] zoneIds;
        protected Map<String, Path2D[]> zoneIdToPolygonsMap = new HashMap<>();
        protected Map<String, Rectangle[]> zoneIdToPolygonBoundingBoxesMap = new HashMap<>();

        /**
         * The Id of the zone in which the last point was found. Useful since mostly side-by-side
         * points will be in the same zone.
         * <p>
         * To identify which zone (polygon) a pixel (lat/lon) is located within, we would need to
         * check every pixel against every polygon, which would be computationally expensive. This
         * workload can be reduced by calculating a bounding box for each polygon, and then check
         * against that bounding box first. If a pixel is within that bounding box, then check if
         * it is actually within the polygon.
         *
         * @see #findZoneId(double, double)
         */
        protected String lastZoneId = null;

        /**
         * Constructor to capture the reference to the underlying map of Zone Id to Polygon.
         */
        public ZoneLookUp(Map<String, Path2D[]> zoneIdToPolygonsMap) {
            super();

            // Cache the zone/region mappings.
            this.zoneIdToPolygonsMap.clear();
            this.zoneIdToPolygonsMap.putAll(zoneIdToPolygonsMap);

            // Build the list of bounding boxes. See notes above that these bounding boxes are only
            // used as the first step to identify POSSIBLE polygons that a point may be within.
            this.zoneIdToPolygonBoundingBoxesMap.clear();
            this.zoneIds = new String[zoneIdToPolygonsMap.keySet().size()];
            int index = 0;
            for (String zoneId : zoneIdToPolygonsMap.keySet()) {
                Path2D[] polygons = zoneIdToPolygonsMap.get(zoneId);
                Rectangle[] boundingBoxes = new Rectangle[polygons.length];
                this.zoneIdToPolygonBoundingBoxesMap.put(zoneId, boundingBoxes);
                for (int polygonIndex = 0; polygonIndex < polygons.length; polygonIndex++) {
                    boundingBoxes[polygonIndex] = polygons[polygonIndex].getBounds();
                }
                this.zoneIds[index] = zoneId;
                index++;
            }
        }

        /**
         * Perform the search, returning the id of the zone/region in which the lon/lat exists. If
         * no zone/region found, then <code>null</code> is returned. This method being the search
         * using the zone/region found for the last search, as normally most side-by-side points
         * are in the same zone/region. If not found in the last zone/region, it the looks through
         * all zones/regions until the match is found, and then caches that value for the next
         * search.
         */
        public String findZoneId(double longitude, double latitude) {

            // If no lookup done previously, start with the first zone/region.
            if (this.lastZoneId == null) {
                this.lastZoneId = this.zoneIds[0];
            }

            // Check the bounding box for the last zone/region checked. If found, check the polygon
            // itself.
            boolean isFound = this.checkZoneId(longitude, latitude, this.lastZoneId);
            if (isFound) {
                return this.lastZoneId;
            } else {
                // Not found, so search through all zones/regions.
                for (String zoneId : this.zoneIdToPolygonsMap.keySet()) {
                    if (this.checkZoneId(longitude, latitude, zoneId)) {
                        this.lastZoneId = zoneId;
                        return zoneId;
                    }
                }
            }

            return null;
        }

        /**
         * Helper method to search for the lon/lat in the specified zone/region.
         *
         * @return <code>true</code> if the lon/lat found in the specified zone/region,
         * <code>false</code> otherwise.
         */
        protected boolean checkZoneId(double longitude, double latitude, String regionId) {
            Rectangle[] boundingBoxes = this.zoneIdToPolygonBoundingBoxesMap.get(regionId);
            if ((boundingBoxes != null) && (boundingBoxes.length > 0)) {
                for (int bbIndex = 0; bbIndex < boundingBoxes.length; bbIndex++) {
                    if (boundingBoxes[bbIndex].contains(longitude, latitude)) {
                        Path2D[] polygons = this.zoneIdToPolygonsMap.get(regionId);
                        for (int polIndex = 0; polIndex < polygons.length; polIndex++) {
                            if (polygons[polIndex].contains(longitude, latitude)) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

    }

}
