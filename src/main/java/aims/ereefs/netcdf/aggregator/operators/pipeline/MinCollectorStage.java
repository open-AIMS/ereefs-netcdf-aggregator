package aims.ereefs.netcdf.aggregator.operators.pipeline;

/**
 * Extends {@link AbstractComparisonCollectorStage} to perform a {@code Min} function across input
 * data.
 *
 * @author Aaron Smith
 */
public class MinCollectorStage extends AbstractComparisonCollectorStage {

    public MinCollectorStage() {
        super(Comparators.LESS_THAN_COMPARATOR);
    }

}