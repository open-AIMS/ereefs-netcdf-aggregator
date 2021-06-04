package aims.ereefs.netcdf.task.aggregation.pipeline;

import ucar.ma2.Array;

import java.util.List;


/**
 * Interface for a {@code Pipeline} {@code Stage} that writes data to an output file/dataset.
 *
 * @author Aaron Smith
 */
public interface WriteTimeSliceStage {

    /**
     * Method invoked to perform the processing of the {@code Stage}.
     */
    void execute(List<Array> arrays, int depthOffset);

}
