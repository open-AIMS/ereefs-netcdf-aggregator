package aims.ereefs.netcdf.task.aggregation.pipeline;

import ucar.ma2.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link WriteTimeSliceStage} to intercept the result for comparison within the
 * test case.
 *
 * @author Aaron Smith
 */
public class WriteTimeSliceInterceptorStage implements WriteTimeSliceStage {

    /**
     * Cached reference to the {@code PipelineContext} for access to context in which the
     * {@code Stage} is operating.
     */
    protected PipelineContext pipelineContext;

    protected List<List<Array>> arraysList = new ArrayList<>();

    public List<List<Array>> getArraysList() {
        return this.arraysList;
    }

    protected List<Integer> depthOffsetList = new ArrayList<>();

    public List<Integer> getDepthOffsetList() {
        return this.depthOffsetList;
    }

    /**
     * Constructor to cache references to static system objects/components.
     */
    public WriteTimeSliceInterceptorStage(PipelineContext pipelineContext) {
        this.pipelineContext = pipelineContext;
    }

    /**
     * Method invoked to perform the processing of the {@code Stage}.
     */
    @Override
    public void execute(List<Array> arrays, int depthOffset) {
        this.arraysList.add(arrays);
        this.depthOffsetList.add(depthOffset);
    }

}
