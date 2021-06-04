package aims.ereefs.netcdf.outputStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.units.DateUnit;


/**
 * OutputFileNameGenerator implementation used to group aggregate data into daily files. This
 * class supports the following text templates in the <code>filenamePattern</code> field:
 *
 * <ul>
 *     <li><b>&lt;date&gt;</b> - the date of the data.</li>
 * </ul>

 * @author Greg Coleman
 * @author Aaron Smith
 */
abstract public class AbstractOutputFileNameGenerator implements OutputFileNameGenerator{

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The template pattern to be used for generating filenames.
     */
    protected String pattern;
    @Override
    public String getPattern() {
        return this.pattern;
    };

    /**
     * Cached reference to a <code>DateUnit</code> for date/time calculations.
     */
    protected DateUnit dateUnit;

    /**
     * Constructor to cache the file pattern to use.
     *
     * @param pattern the pattern to use when generating the output filename.
     */
    public AbstractOutputFileNameGenerator(String pattern, DateUnit dateUnit) {
        super();
        this.pattern = pattern;
        this.dateUnit = dateUnit;
    }

}
