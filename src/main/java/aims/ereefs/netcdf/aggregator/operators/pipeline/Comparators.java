package aims.ereefs.netcdf.aggregator.operators.pipeline;

import java.util.function.BiPredicate;

/**
 * Convenience class grouping supported comparators for use by the various {@code Pipeline}
 * {@link Stage}s.
 *
 * @author Aaron Smith
 */
public class Comparators {

    /**
     * A {@code Comparator} that returns {@code true} if {@code d1} is {@code GREATER} than
     * {@code d2}.
     */
    public static final BiPredicate<Double, Double> GREATER_THAN_COMPARATOR = (d1, d2) -> d1 > d2;

    /**
     * A {@code Comparator} that returns {@code true} if {@code d1} is {@code LESS} than
     * {@code d2}.
     */
    public static final BiPredicate<Double, Double> LESS_THAN_COMPARATOR = (d1, d2) -> d1 < d2;

}
