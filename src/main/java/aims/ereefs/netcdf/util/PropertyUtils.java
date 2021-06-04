package aims.ereefs.netcdf.util;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utilities for reading/writing <code>properties</code>, where a <code>property</code> is a
 * key/value pair, with <code>key</code> being a unique <code>String</code> and <code>value</code>
 * being a <code>String</code> value that may represent a number.
 *
 * @author Aaron Smith
 */
public class PropertyUtils {

    /**
     * Return the value of the identified <code>String</code> property, or the specified default
     * value if not found.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @param defaultValue the value to return if the named property is not found.
     * @return the value of the property, or <code>defaultValue</code> if not found.
     */
    static public String getPropertyOrDefault(Map<String, String> properties,
                                              String name,
                                              String defaultValue) {
        for (String key : properties.keySet()) {
            if (key.equalsIgnoreCase(name)) {
                return properties.get(key);
            }
        }
        return defaultValue;
    }

    /**
     * Return the value of the identified <code>String</code> property, or throw a
     * <code>RuntimeException</code> if not found.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @return the value of the property.
     * @throws NoSuchElementException thrown if property not found.
     */
    static public String getProperty(Map<String, String> properties, String name)
        throws RuntimeException {
        String value = getPropertyOrDefault(properties, name, null);
        if (value != null) {
            return value;
        } else {
            throw new NoSuchElementException("Property \"" + name + "\" not found.");
        }
    }

    /**
     * Return the value of the identified <code>Double</code> property, or throw a
     * <code>RuntimeException</code> if not found or it is not a <code>Double</code>.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @return the value of the property.
     * @throws RuntimeException thrown if property not found.
     */
    static public double getPropertyAsDouble(Map<String, String> properties, String name)
    throws RuntimeException {
        try {
            return Double.valueOf(getProperty(properties, name));
        } catch(NumberFormatException nfe) {
            throw new RuntimeException("Property \"" + name + "\" is not a double.", nfe);
        }
    }

    /**
     * Return the value of the identified <code>Integer</code> property, or throw a
     * <code>RuntimeException</code> if not found or it is not an <code>Integer</code>.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @return the value of the property.
     * @throws RuntimeException thrown if property not found.
     */
    static public int getPropertyAsInteger(Map<String, String> properties, String name) {
        try {
            return Integer.valueOf(getProperty(properties, name));
        } catch(NumberFormatException nfe) {
            throw new RuntimeException("Property \"" + name + "\" is not an integer.", nfe);
        }
    }

    /**
     * Return the value of the identified <code>Boolean</code> property, or throw a
     * <code>RuntimeException</code> if not found or it is not a <code>Boolean</code>.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @return the value of the property.
     * @throws RuntimeException thrown if property not found.
     */
    static public boolean getPropertyAsBoolean(Map<String, String> properties, String name) {
        try {
            return Boolean.valueOf(getProperty(properties, name));
        } catch(NumberFormatException nfe) {
            throw new RuntimeException("Property \"" + name + "\" is not a boolean.", nfe);
        }
    }

    /**
     * Return the value of the identified <code>Boolean</code> property, or the specified default
     * value if not found.
     *
     * @param properties the list of <code>properties</code>.
     * @param name the unique key to search for.
     * @param defaultValue the value to return if the named property is not found.
     * @return the value of the property, or <code>defaultValue</code> if not found.
     */
    static public boolean getPropertyAsBoolean(Map<String, String> properties,
                                               String name,
                                               boolean defaultValue) {
        try {
            return Boolean.valueOf(getProperty(properties, name));
        } catch(NumberFormatException | NoSuchElementException ignore) {
            return defaultValue;
        }
    }

}
