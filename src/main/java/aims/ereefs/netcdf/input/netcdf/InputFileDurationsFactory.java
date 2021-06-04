package aims.ereefs.netcdf.input.netcdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class responsible for identifying the {@link InputFileDurations} specified.
 *
 * @author Greg Coleman
 * @author Aaron Smith
 */
public class InputFileDurationsFactory {

    static private Logger logger = LoggerFactory.getLogger(InputFileDurationsFactory.class);

    public static InputFileDurations make(String id) {
        logger.debug("id: " + id);
        for (InputFileDurations d : InputFileDurations.values()) {
            if (d.id.equalsIgnoreCase(id)) {
                return d;
            }
        }
        throw new RuntimeException("InputFileDuration \"" + id + "\" not supported.");

    }
}
