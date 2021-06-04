package aims.ereefs.netcdf.util;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.AWSSimpleSystemsManagementException;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;

/**
 * Utility class for reading configuration properties from the SSM Parameter Store.
 *
 * @author Aaron Smith
 */
public class ParameterStoreReader {

    /**
     * Cache the {@code ExecutionEnvironment} if specified.
     */
    static private String executionEnvironment = null;

    static public void setExecutionEnvironment(String executionEnvironment) {
        ParameterStoreReader.executionEnvironment = executionEnvironment;
    }

    /**
     * Singleton connection object to the AWS Parameter Store.
     */
    static private AWSSimpleSystemsManagement simpleSystemsManagement = null;

    static private AWSSimpleSystemsManagement getSSMInstance() {
        if (simpleSystemsManagement == null) {
            simpleSystemsManagement = AWSSimpleSystemsManagementClientBuilder.standard().build();
        }
        return simpleSystemsManagement;
    }

    /**
     * Returns the value assigned to the mandatory key. If an environment variable is not found
     * (is {@code null}), a {@code RuntimeException} is thrown.
     *
     * This method first attempts to retrieve a value assigned to {@code key} WITHOUT prefixing the
     * {@link #executionEnvironment}, and if that fails, the {@link #executionEnvironment} is
     * prefixed to the {@code key}. This is a convenience feature as it removes the need to add
     * {@link #executionEnvironment} to every {@code key} throughout the code base. However, if
     * the permissions assigned to the execution role are specific, this two (2) step lookup could
     * trigger an {@code AccessDeniedException} on the first attempt. For this reason, the
     * {@code AccessDeniedException} (actually *ALL* AWS SSM exceptions) are ignored on the first
     * attempt.
     */
    static public String getByKey(String key) {

        // Attempt to retrieve the parameter value without the execution environment.
        GetParameterResult result = null;
        try {
            result = ParameterStoreReader.getSSMInstance().getParameter(
                new GetParameterRequest().withName(key)
            );
        } catch (ParameterNotFoundException ignore) {
            // Key didn't exist, so ignore the exception from this attempt.
        } catch (AWSSimpleSystemsManagementException ignore) {
            // Execution role does not have permission to attempt to retrieve this key. Assume this
            // is because the ExecutionEnvironment is missing as a prefix to the key. If something
            // else is at play here, it will be reported in the next attempt to retrieve the
            // parameter.
        }

        // If not found, try again WITH the execution environment.
        if (result == null) {
            result = ParameterStoreReader.getSSMInstance().getParameter(
                new GetParameterRequest().withName("/" + ParameterStoreReader.executionEnvironment + key)
            );
        }

        // Extract the value if an entry was found.
        if (result != null) {
            return result.getParameter().getValue();
        }

        // Throw an error if no entry was found. Normally this code will not be reached, as the last attempt WITH the
        // execution environment would have thrown a "ParameterNotFoundException", or a "AccessDeniedException".
        throw new RuntimeException("Parameter \"" + key + "\" is mandatory.");
    }

}
