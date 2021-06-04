package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.List;

/**
 * Basic implementation of the {@link Pipeline} interface that allows for a single, multi-variable
 * {@link Collector}.
 *
 * @author Aaron Smith
 */
public class BasicPipeline implements Pipeline {

    protected Stage initialStage;
    protected Collector finalCollector;

    public BasicPipeline(Stage initialStage,
                         Collector finalCollector) {
        this.initialStage = initialStage;
        this.finalCollector = finalCollector;
    }

    @Override
    public void execute(List<Double[]> inputs) {
        this.initialStage.execute(inputs);
    }

    @Override
    public void reset() {
        this.initialStage.reset();
    }

    @Override
    public List<Double[]> getResults() {
        return this.finalCollector.getResults();
    }

}