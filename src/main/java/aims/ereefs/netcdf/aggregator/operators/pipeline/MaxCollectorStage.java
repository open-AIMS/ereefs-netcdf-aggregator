package aims.ereefs.netcdf.aggregator.operators.pipeline;

/**
 * Extends {@link AbstractComparisonCollectorStage} to perform a {@code Max} function across input
 * data.
 *
 * @author Aaron Smith
 */
public class MaxCollectorStage extends AbstractComparisonCollectorStage {

    public MaxCollectorStage() {
        super(Comparators.GREATER_THAN_COMPARATOR);
    }

}