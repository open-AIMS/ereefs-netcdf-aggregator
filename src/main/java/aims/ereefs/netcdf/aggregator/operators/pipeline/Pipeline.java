package aims.ereefs.netcdf.aggregator.operators.pipeline;

/**
 * Public interface for a class that wraps a multi-{@link Stage} {@code Pipeline}, the starts with
 * a {@link Stage} implementation and completes with one or more {@link Collector} implementations,
 * simplifying handling. This interface merely combines the {@link Stage} and {@link Collector}
 * interfaces.
 *
 * @author Aaron Smith
 */
public interface Pipeline extends Stage, Collector {
}