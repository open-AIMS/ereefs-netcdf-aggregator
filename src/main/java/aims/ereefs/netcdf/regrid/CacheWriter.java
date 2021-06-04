package aims.ereefs.netcdf.regrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.io.*;
import java.util.*;

/**
 * Utility class for writing the {@code RegularGridToCurvedIndex} map to a cache file.
 *
 * @author Aaron Smith
 */
public class CacheWriter {

    private static Logger logger = LoggerFactory.getLogger(CacheWriter.class);

    // The version of caching supported by this writer.
    static final public double CACHE_VERSION = 1.0;

    /**
     * Perform the write.
     *
     * @param regularGridToCurvedIndex the {@code Map} to cache.
     * @param cacheFilename The filename (including path) of the cache file to create.
     */
    static public void write(Map<Point, IndexWithDistance[]> regularGridToCurvedIndex,
                             String cacheFilename) {

        logger.debug("Caching mappings to \"" + cacheFilename + "\".");
        File cacheFile = new File(cacheFilename);
        try {
            try (DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(cacheFile)))) {
                outputStream.writeInt(regularGridToCurvedIndex.size());
                for (Map.Entry<Point, IndexWithDistance[]> entry : regularGridToCurvedIndex.entrySet()) {
                    Point point = entry.getKey();
                    outputStream.writeInt(point.getX());
                    outputStream.writeInt(point.getY());
                    IndexWithDistance[] list = entry.getValue();
                    outputStream.writeInt(list.length);
                    for (IndexWithDistance item : list) {
                        outputStream.writeInt(item.getIndex());
                        outputStream.writeDouble(item.getDistance());
                    }
                }
                logger.debug("Cached.");
            }
        } catch (Exception ignore) {
            logger.warn("Failed to write cache (\"" + cacheFilename + "\").", ignore);
            throw new RuntimeException(ignore);
        }
    }

    /**
     * Perform the write.
     */
    static public void write(RegularGridMapper regularGridMapper,
                             String cacheFilename) {

        logger.debug("Caching mappings to \"" + cacheFilename + "\".");
        File cacheFile = new File(cacheFilename);
        try {
            try (DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(cacheFile)))) {

                outputStream.writeDouble(CACHE_VERSION);

                final int latitudeCount = regularGridMapper.getLatitudeCount();
                logger.debug("latitudeCount: " + latitudeCount);
                outputStream.writeInt(latitudeCount);

                final int longitudeCount = regularGridMapper.getLongitudeCount();
                logger.debug("longitudeCount: " + longitudeCount);
                outputStream.writeInt(longitudeCount);

                Array outputLatitudeArray = regularGridMapper.getOutputLatitudeArray();
                for (int latitudeIndex = 0; latitudeIndex < latitudeCount; latitudeIndex++) {
                    outputStream.writeDouble(outputLatitudeArray.getDouble(latitudeIndex));
                }

                Array outputLongitudeArray = regularGridMapper.getOutputLongitudeArray();
                for (int longitudeIndex = 0; longitudeIndex < longitudeCount; longitudeIndex++) {
                    outputStream.writeDouble(outputLongitudeArray.getDouble(longitudeIndex));
                }

                final Map<Point, IndexWithDistance[]> regularGridToCurvedIndex =
                    regularGridMapper.getRegularGridToCurvedIndex();
                logger.debug("size: " + regularGridToCurvedIndex.size());
                outputStream.writeInt(regularGridToCurvedIndex.size());
                for (Map.Entry<Point, IndexWithDistance[]> entry : regularGridToCurvedIndex.entrySet()) {
                    Point point = entry.getKey();
                    outputStream.writeInt(point.getX());
                    outputStream.writeInt(point.getY());
                    IndexWithDistance[] list = entry.getValue();
                    outputStream.writeInt(list.length);
                    for (IndexWithDistance item : list) {
                        outputStream.writeInt(item.getIndex());
                        outputStream.writeDouble(item.getDistance());
                    }
                }
                logger.debug("Cached.");

            }
        } catch (Exception ignore) {
            logger.warn("Failed to write cache (\"" + cacheFilename + "\").", ignore);
            throw new RuntimeException(ignore);
        }
    }

}