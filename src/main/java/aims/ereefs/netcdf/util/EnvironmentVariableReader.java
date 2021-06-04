package aims.ereefs.netcdf.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for reading environment data.
 *
 * @author Aaron Smith
 */
public class EnvironmentVariableReader {

    static final public String ENV_PREFIX = "NCAGGREGATE";

    /**
     * Cached reference to the {@code Environment} from which to retrieve parameters.
     */
    private Map<String, String> cachedEnvironment = null;

    /**
     * Singleton instance of the {@link EnvironmentVariableReader}.
     */
    static private EnvironmentVariableReader SINGLETON = null;

    /**
     * Returns the {@link #SINGLETON} instance, instantiating it if necessary.
     */
    static final public EnvironmentVariableReader getInstance() {

        // Build a complete list of environment variables/properties.
        final Map<String, String> env = new HashMap<>();

        env.putAll(System.getenv());

        final Properties properties = System.getProperties();
        for (final Object key : properties.keySet()) {
            env.put(key.toString(), properties.getProperty(key.toString()));
        }

        return getInstance(env);
    }

    /**
     * Returns the {@link #SINGLETON} instance, instantiating it with the specified
     * {@code environment} if necessary.
     */
    static final public EnvironmentVariableReader getInstance(Map<String, String> env) {
        if (SINGLETON == null) {
            SINGLETON = new EnvironmentVariableReader(env);
        }
        return SINGLETON;
    }

    /**
     * Delete the existing {@code singleton} instance, resulting in the next invocation of
     * {@link #getInstance()} or {@link #getInstance(Map)} instantiating a new {@code singleton}
     * instance.
     */
    static final public void clearInstance() {
        SINGLETON = null;
    }

    /**
     * Constructor for the {@link EnvironmentVariableReader}, caching the {@code Map} to use for
     * the environment.
     */
    private EnvironmentVariableReader(Map<String, String> environment) {
        this.cachedEnvironment = environment;
    }

    /**
     * Returns the value assigned to the mandatory key. If an environment variable is not found
     * (is {@code null}), a {@code RuntimeException} is thrown.
     */
    public String getByKey(String key) {
        final String value = this.optByKey(key);
        if (value == null) {
            throw new RuntimeException("Environment variable \"" + ENV_PREFIX + "." + key + "\" is mandatory.");
        }
        return value;
    }

    /**
     * Returns the value assigned to the optional key, or {@code null} if not found.
     */
    public String optByKey(String key) {
        String value = this.cachedEnvironment.get(ENV_PREFIX + "." + key);
        if (value == null) {
            value = this.cachedEnvironment.get(key);
        }
        return value;
    }

}