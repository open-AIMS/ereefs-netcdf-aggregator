package aims.ereefs.netcdf;

/**
 * Interface for classes that perform the action of the application. Implementation classes have
 * access to all of the classes and most of the stack of the application, allowing for
 * similar but quite different actions to be performed within the framework of the application.
 * Refer to implementing classes for more information.
 *
 * @author Aaron Smith
 */
public interface OperationModeExecutor {

    /**
     * Invoked by the caller to determine if the implementing class supports execution based on the
     * specified arguments.
     *
     * @return {@code true} if the implementing class should handle the execution, {@code false}
     * otherwise.
     */
    public boolean supports(String[] args);

    /**
     * Invoked by the caller to perform the actions of the implementing class. Note that this method
     * should only be invoked if {@link #supports(String[])} returned {@code true}.
     */
    public void execute(String[] args);

}
