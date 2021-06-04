package aims.ereefs.netcdf.input.netcdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class responsible for identifying the {@link InputFileTypes} specified.
 *
 * @author Greg Coleman
 */
public class InputFileTypesFactory {

    static private Logger logger = LoggerFactory.getLogger(InputFileTypesFactory.class);

    public static InputFileTypes make(String id) {
        logger.debug("id: " + id);
        switch (id.toLowerCase()) {
            case "geojson":
                return InputFileTypes.GEOJSON;
            case "netcdf":
            default:
                return InputFileTypes.NETCDF;
        }
    }
}
