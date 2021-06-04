package aims.ereefs.netcdf;

/**
 * Interface for classes that perform the execution of the application based on the operation mode
 * specified via command line arguments.
 *
 * @author Aaron Smith
 */
public interface OperationModeExecutor {

    /**
     * Test method to determine if the implementing class should handle execution of the
     * application.
     *
     * @return {@code true} if the implementing class should handle the execution, {@code false}
     * otherwise.
     */
    public boolean supports(String[] args);

    /**
     * Performs the actions required by the operation mode. This method should only be invoked if
     * {@link #supports(String[])} returned {@code true}.
     */
    public void execute(String[] args);

}
