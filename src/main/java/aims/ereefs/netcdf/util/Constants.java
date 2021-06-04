package aims.ereefs.netcdf.util;

/**
 * Simple centralisation of {@code CONSTANTS} used in multiple places within the system.
 */
public class Constants {

    /**
     * The {@code Separator} used between an {@code InputSourceId} and the
     * {@code ShortVariableName} to define a fully qualified variable name. For example:
     * {@code "{InputSourceId}{separator}{ShortVariableName}"}.
     */
    static public String VARIABLE_NAME_SEPARATOR = "::";

}
