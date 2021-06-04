package aims.ereefs.netcdf.output.summary;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.io.IOException;

/**
 * A generic interface for classes that abstract storage of the {@link SummaryStatistics}.
 *
 * @author Aaron Smith
 */
public interface OutputWriter {

    /**
     * Write the data to the summary output file.
     *
     * @param dateTimeStamp     a {@code String} presentation of the date/time.
     * @param summaryOperator   reference to the object for identifying processing information,
     * @param depth             the depth for the data being written.
     * @param zoneId            the zone for the data being written.
     * @param summaryStatistics the {@link SummaryStatistics} object to be written.
     */
    void write(String dateTimeStamp, NcAggregateProductDefinition.SummaryOperator summaryOperator,
               double depth, String zoneId, SummaryStatistics summaryStatistics) throws IOException;

    /**
     * Flush any data that has not yet been written to the underlying file.
     */
    void flush() throws IOException;

    /**
     * Close/terminate any system resources associated with the underlying file.
     */
    void close() throws IOException;

}
