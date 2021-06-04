package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.regrid.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@code Utility} class for building a map containing all of the coordinates from a reference
 * dataset.
 *
 * @author Aaron Smith
 */
public class CoordinateMapBuilder {

    static protected Logger logger = LoggerFactory.getLogger(CoordinateMapBuilder.class);

    static public Map<Integer, Coordinate> build(Array latitudeArray,
                                                 Array longitudeArray,
                                                 boolean isRectilinearGrid) {

        final Map<Integer, Coordinate> map = new TreeMap<>();

        if (isRectilinearGrid) {

            logger.debug("Treating as a rectilinear grid.");

            // For rectilinear grids, the standard ordering we are following is latitude then
            // longitude.
            long latSize = latitudeArray.getSize();
            long lonSize = longitudeArray.getSize();
            int cellIndex = 0;
            for (int latIndex = 0; latIndex < latSize; latIndex++) {
                double latitude = latitudeArray.getDouble(latIndex);
                for (int lonIndex = 0; lonIndex < lonSize; lonIndex++) {
                    double longitude = longitudeArray.getDouble(lonIndex);
                    map.put(cellIndex, new Coordinate(latitude, longitude));
                    cellIndex++;
                }
            }

        } else {

            logger.debug("Treating as a curvilinear grid.");

            long size = longitudeArray.getSize();
            for (int index = 0; index < size; index++) {
                map.put(
                    index,
                    new Coordinate(
                        latitudeArray.getDouble(index),
                        longitudeArray.getDouble(index)
                    )
                );
            }
        }

        return map;

    }

}
