package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link Pipeline} interface that allows for multiple, single-variable
 * {@link Collector}s.
 *
 * @author Aaron Smith
 */
public class CombiningPipeline implements Pipeline {

    protected Stage initialStage;
    protected List<Collector> collectors;

    public CombiningPipeline(Stage initialStage,
                             List<Collector> collectors) {
        this.initialStage = initialStage;
        this.collectors = collectors;
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
        final List<Double[]> results = new ArrayList<>();
        for (Collector collector : this.collectors) {
            results.add(collector.getResults().get(0));
        }
        return results;
    }

}