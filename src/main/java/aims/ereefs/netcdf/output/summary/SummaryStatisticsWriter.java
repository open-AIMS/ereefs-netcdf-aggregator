package aims.ereefs.netcdf.output.summary;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Utility class responsible for co-ordinating analysis ({@link SummaryStatisticsCalculator} of the
 * data accumulated by the {@link SummaryAccumulator} and writing via the {@link OutputWriter}
 * implementation.
 *
 * @author Aaron Smith
 */
public class SummaryStatisticsWriter {

    static public void write(LocalDateTime dateTime,
                             NcAggregateProductDefinition.SummaryOperator summaryOperator,
                             SummaryAccumulator summaryAccumulator,
                             OutputWriter writer) throws IOException {

        String dateTimeStamp = dateTime.toString();

        // Perform the analysis of the accumulated data.
        Map<Double, Map<String, List<Double>>> depthToAccumulationBucketsMap =
            summaryAccumulator.getDepthToAccumulationBucketsMap();
        for (Double depth : depthToAccumulationBucketsMap.keySet()) {
            Map<String, List<Double>> accumulationBuckets = depthToAccumulationBucketsMap.get(depth);
            for (String id : accumulationBuckets.keySet()) {
                List<Double> accumulationBucket = accumulationBuckets.get(id);
                SummaryStatistics summaryStatistics =
                    SummaryStatisticsCalculator.calculate(accumulationBucket);
                writer.write(
                    dateTimeStamp,
                    summaryOperator,
                    depth,
                    id,
                    summaryStatistics
                );
            }
        }
    }

}
