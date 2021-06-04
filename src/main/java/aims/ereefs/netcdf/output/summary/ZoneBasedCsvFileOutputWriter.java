package aims.ereefs.netcdf.output.summary;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.input.SimpleDataset;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A concrete implementation of {@link OutputWriter} for writing to a CSV file.
 *
 * @author Aaron Smith
 */
public class ZoneBasedCsvFileOutputWriter extends AbstractCsvFileOutputWriter {

    /**
     * Cached reference to the {@link ApplicationContext}.
     */
    protected ApplicationContext applicationContext;

    public ZoneBasedCsvFileOutputWriter(File outputFile,
                                        ApplicationContext applicationContext,
                                        String zoneNamesBindName) throws IOException {
        super(outputFile);

        // Cache the ApplicationContext for use later.
        this.applicationContext = applicationContext;

        // Retrieve the lookup dataset from the cache.
        final SimpleDataset lookupDataset =
            (SimpleDataset) this.applicationContext.getFromCache(zoneNamesBindName);

        // Package the dataset for easy lookup.
        for (String[] record : lookupDataset.getData()) {
            this.idToDetailsMap.put(record[0], record);
        }

        // Add the header record.
        this.fileWriter.write(
            "\"Aggregated Date/Time\"," +
                "\"Variable\"," +
                "\"Depth\"," +
                "\"Zone Id\"," +
                "\"Zone Name\"," +
                "\"Threshold\"," +
                "\"mean\"," +
                "\"median\"," +
                "\"p5\"," +
                "\"p95\"," +
                "\"lowest\"," +
                "\"highest\"" +
                "\n"
        );

    }

    @Override
    public void write(String dateTimeStamp, NcAggregateProductDefinition.SummaryOperator summaryOperator,
                      double depth, String zoneId, SummaryStatistics summaryStatistics) throws IOException {

        // Identify the variableName to output.
        final String variableName =
            summaryOperator.getOutputVariables().get(0).getAttributes().get("short_name");

        // Identify the threshold value for the zone.
        Double zoneThreshold = Double.NaN;
        if (summaryOperator instanceof NcAggregateProductDefinition.ThresholdZonalSummaryOperator) {
            NcAggregateProductDefinition.ThresholdZonalSummaryOperator thresholdZonalSummaryOperator =
                (NcAggregateProductDefinition.ThresholdZonalSummaryOperator) summaryOperator;

            // Retrieve the ZoneIdToThreshold map. No need to check before typecasting because this
            // MUST be correct in the config to proceed.
            final Map<String, Double[]> zoneIdToThresholdMap =
                (Map<String, Double[]>) this.applicationContext.getFromCache(
                    thresholdZonalSummaryOperator.getThresholdsInputId()
                );

            // Identify the threshold value for this zone.
            Double[] thresholds = zoneIdToThresholdMap.get(zoneId);
            if (thresholds.length > 0) {
                zoneThreshold = thresholds[0];
            }
        }

        // Retrieve the lookup record for the zone/site.
        final String[] record = this.idToDetailsMap.get(zoneId);
        this.fileWriter.write(
            dateTimeStamp + "," +
                "\"" + variableName + "\"," +
                "\"" + depth + "\"," +
                "\"" + zoneId + "\"," +
                "\"" + record[1] + "\"," +
                (!zoneThreshold.isNaN() ? zoneThreshold : "unknown") + "," +
                (summaryStatistics != null ? summaryStatistics.getMean() : "no data") + "," +
                (summaryStatistics != null ? summaryStatistics.getMedian() : "no data") + "," +
                (summaryStatistics != null ? summaryStatistics.getLowPercentile() : "no data") + "," +
                (summaryStatistics != null ? summaryStatistics.getHighPercentile() : "no data") + "," +
                (summaryStatistics != null ? summaryStatistics.getLowest() : "no data") + "," +
                (summaryStatistics != null ? summaryStatistics.getHighest() : "no data") +
                "\n"
        );
    }

}
