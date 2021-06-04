package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.regrid.RegularGridMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@code Pipeline} {@code Stage} converting data from a curvilinear grid to a regular grid.
 *
 * @author Aaron Smith
 */
public class RegularGriddingStage {

    /**
     * Class-specific {@code logger}.
     */
    protected Logger logger = LoggerFactory.getLogger(RegularGriddingStage.class);

    /**
     * Cached reference to the {@code PipelineContext} for access to context in which the
     * {@code Stage} is operating.
     */
    protected PipelineContext pipelineContext;

    /**
     * Cached reference to the {@code RegularGridMapper} for performing the regridding.
     */
    protected RegularGridMapper regularGridMapper;

    /**
     * Cached reference to the next {@code Stage} to invoke.
     */
    protected WriteTimeSliceStage writeTimeSliceStage;

    /**
     * Constructor to cache references to static system objects/components.
     */
    public RegularGriddingStage(PipelineContext pipelineContext,
                                RegularGridMapper regularGridMapper,
                                WriteTimeSliceStage writeTimeSliceStage) {
        this.pipelineContext = pipelineContext;
        this.regularGridMapper = regularGridMapper;
        this.writeTimeSliceStage = writeTimeSliceStage;
    }

    /**
     * Method invoked to perform the processing of the {@code Stage}.
     */
    public void execute(List<Array> inputArrays, int depthIndex) {


        if (this.regularGridMapper == null) {

            // Not regridding.
            this.writeTimeSliceStage.execute(inputArrays, depthIndex);

        } else {

            // Perform regridding.
            final List<Array> regriddedArrays = new ArrayList<>();
            for (Array array : inputArrays) {
                regriddedArrays.add(this.regularGridMapper.curvedToRegular(array));
            }
            this.writeTimeSliceStage.execute(regriddedArrays, depthIndex);

        }

    }
}
