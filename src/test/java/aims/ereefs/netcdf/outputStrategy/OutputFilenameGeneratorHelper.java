package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.util.DateUtils;
import org.assertj.core.api.Assertions;

/**
 * Helper methods and properties for testing {@link OutputFileNameGenerator} classes.
 *
 * @author Aaron Smith
 */
public class OutputFilenameGeneratorHelper {

    /**
     * Standard filename to use in tests.
     */
    static public final String FILENAME = "filename.nc";

    /**
     * Standard filename pattern to use in tests.
     */
    static public final String PATTERN_PREFIX = "pre.";
    static public final String PATTERN_POSTFIX = ".post";

    /**
     * Helper method to instantiate an {@link OutputFileNameGenerator} class for use by tests.
     */
    final static public OutputFileNameGenerator makeGenerator(OutputFileStrategy strategy,
                                                              String pattern,
                                                              Class expectedClass) {
        OutputFileNameGenerator generator = OutputFilenameGeneratorFactory.make(
            strategy,
            pattern,
            DateUtils.getDateUnit());
        Assertions
            .assertThat(generator)
            .isInstanceOf(expectedClass);
        Assertions
            .assertThat(generator.getPattern())
            .isEqualTo(pattern);
        return generator;
    }

}
