package aims.ereefs.netcdf.output.summary;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.io.IOException;

/**
 * A concrete implementation of {@link OutputWriter} that does nothing.
 *
 * @author Aaron Smith
 */
public class NullOutputWriter implements OutputWriter {

    @Override
    public void write(String dateTimeStamp, NcAggregateProductDefinition.SummaryOperator summaryOperator,
                      double depth, String zoneId, SummaryStatistics summaryStatistics) throws IOException {
        // Do Nothing.
    }

    @Override
    public void flush() throws IOException {
        // Do nothing.
    }

    @Override
    public void close() throws IOException {
        // Do nothing.
    }

}
