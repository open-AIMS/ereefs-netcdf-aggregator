package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.output.netcdf.OutputDataset;
import aims.ereefs.netcdf.util.netcdf.WriteUtils;
import ucar.ma2.Array;

import java.util.List;


/**
 * A {@code Pipeline} {@code Stage} that writes data to an output file/dataset.
 *
 * @author Aaron Smith
 */
public class WriteTimeSliceToOutputDatasetStage implements WriteTimeSliceStage {

    /**
     * Cached reference to the {@code PipelineContext} for access to context in which the
     * {@code Stage} is operating.
     */
    protected PipelineContext pipelineContext;

    /**
     * Cached reference to the {@link OutputDataset} being populated.
     */
    protected OutputDataset outputDataset;

    /**
     * Constructor to cache references to static system objects/components.
     */
    public WriteTimeSliceToOutputDatasetStage(PipelineContext pipelineContext,
                                              OutputDataset outputDataset) {
        this.pipelineContext = pipelineContext;
        this.outputDataset = outputDataset;
    }

    /**
     * Method invoked to perform the processing of the {@code Stage}.
     */
    @Override
    public void execute(List<Array> arrays, int depthOffset) {
        // Do nothing if an output dataset is not specified.
        if (this.outputDataset != null) {
            WriteUtils.writeSlice(
                this.outputDataset,
                pipelineContext.getSummaryOperator(),
                arrays,
                pipelineContext.getTimeInstantIndex(),
                depthOffset
            );
        }
    }

}
