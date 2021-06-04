package aims.ereefs.netcdf.regrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for reading the {@code RegularGridToCurvedIndex} map from a cache file.
 *
 * @author Aaron Smith
 */
public class CacheReader {

    private static Logger logger = LoggerFactory.getLogger(CacheReader.class);

    /**
     * Perform the read.
     *
     * @param cacheFilename The filename (including path) of the cache file to use.
     * @return the map if successfully populated, {@code null} otherwise.
     */
    static public Map<Point, IndexWithDistance[]> read(String cacheFilename) {

        // Only populate if a cache file is specified.
        if (cacheFilename == null) {
            logger.warn("Filename for cache not specified.");
            return null;
        }

        // Ensure the file exists.
        File cacheFile = new File(cacheFilename);
        if (!cacheFile.exists()) {
            logger.warn("Cache file does not exist.");
            return null;
        }
        if (!cacheFile.isFile()) {
            logger.warn("Cache file is not a file.");
            return null;
        }
        logger.debug("Loading RegularGridMapper from \"" + cacheFilename + "\".");

        // Declare the temporary map to populate.
        final Map<Point, IndexWithDistance[]> regularGridToCurvedIndex = new HashMap<>();

        try {
            try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(cacheFile)))) {
                int size = inputStream.readInt();
                for (int entryIndex = 0; entryIndex < size; entryIndex++) {
                    int x = inputStream.readInt();
                    int y = inputStream.readInt();
                    Point point = new Point(x, y);
                    int listSize = inputStream.readInt();
                    IndexWithDistance[] list = new IndexWithDistance[listSize];
                    for (int listIndex = 0; listIndex < listSize; listIndex++) {
                        int index = inputStream.readInt();
                        double distance = inputStream.readDouble();
                        list[listIndex] = new IndexWithDistance(index, distance);
                    }
                    regularGridToCurvedIndex.put(point, list);
                    if (entryIndex % 10000 == 0) {
                        logger.info("Count " + entryIndex + " of " + size);
                    }
                }

                logger.debug("Loaded: " + regularGridToCurvedIndex.size());

                return regularGridToCurvedIndex;
            }
        } catch (Exception ignore) {
            logger.warn("Failed to load cache (\"" + cacheFilename + "\").", ignore);
            return null;
        }

    }

    /**
     * Perform the read.
     *
     * @param cacheFilename The filename (including path) of the cache file to use.
     * @return the map if successfully populated, {@code null} otherwise.
     */
    static public RegularGridMapper readAsRegularGridMapper(String cacheFilename) {

        // Only populate if a cache file is specified.
        if (cacheFilename == null) {
            logger.warn("Filename for cache not specified.");
            return null;
        }

        // Ensure the file exists.
        File cacheFile = new File(cacheFilename);
        if (!cacheFile.exists()) {
            logger.warn("Cache file does not exist.");
            return null;
        }
        if (!cacheFile.isFile()) {
            logger.warn("Cache file is not a file.");
            return null;
        }
        logger.debug("Loading RegularGridMapper from \"" + cacheFilename + "\".");

        // Declare the temporary map to populate.
        Map<Point, IndexWithDistance[]> regularGridToCurvedIndex = null;

        try {
            try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(cacheFile)))) {

                final double version = inputStream.readDouble();
                logger.trace("version: " + version);
                if (version != 1.0) {
                    throw new RuntimeException(
                        "Version not supported. Expected \"1.0\" but found \"" + version + "\".");
                }

                final int latitudeCount = inputStream.readInt();
                logger.trace("latitudeCount: " + latitudeCount);

                final int longitudeCount = inputStream.readInt();
                logger.trace("longitudeCount: " + longitudeCount);

                final Array outputLatitudeArray = new ArrayDouble.D1(latitudeCount);
                for (int latitudeIndex = 0; latitudeIndex < latitudeCount; latitudeIndex++) {
                    double latitude = inputStream.readDouble();
                    outputLatitudeArray.setDouble(latitudeIndex, latitude);
                }

                final Array outputLongitudeArray = new ArrayDouble.D1(longitudeCount);
                for (int longitudeIndex = 0; longitudeIndex < longitudeCount; longitudeIndex++) {
                    double longitude = inputStream.readDouble();
                    outputLongitudeArray.setDouble(longitudeIndex, longitude);
                }

                int size = inputStream.readInt();
                regularGridToCurvedIndex = new HashMap<>(size);
                logger.debug("size: " + size);
                for (int entryIndex = 0; entryIndex < size; entryIndex++) {
                    int x = inputStream.readInt();
                    int y = inputStream.readInt();
                    Point point = new Point(x, y);
                    int listSize = inputStream.readInt();
                    if (listSize > 0) {
                        IndexWithDistance[] list = new IndexWithDistance[listSize];
                        for (int listIndex = 0; listIndex < listSize; listIndex++) {
                            int index = inputStream.readInt();
                            double distance = inputStream.readDouble();
                            list[listIndex] = new IndexWithDistance(index, distance);
                        }
                        regularGridToCurvedIndex.put(point, list);
                    }
                    if (logger.isDebugEnabled()) {
                        if (entryIndex % 10000 == 0) {
                            logger.debug("Count " + entryIndex + " of " + size);
                        }
                    }
                }

                logger.debug("Loaded: " + regularGridToCurvedIndex.size());

                return new RegularGridMapper(latitudeCount, longitudeCount, outputLatitudeArray,
                    outputLongitudeArray, regularGridToCurvedIndex);
            }
        } catch (Exception ignore) {
            logger.warn("Failed to load cache (\"" + cacheFilename + "\").", ignore);
            return null;
        }

    }

}