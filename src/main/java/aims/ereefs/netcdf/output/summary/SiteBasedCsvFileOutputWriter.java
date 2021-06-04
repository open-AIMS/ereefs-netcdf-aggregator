package aims.ereefs.netcdf.output.summary;

import aims.ereefs.netcdf.input.extraction.ExtractionSite;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A concrete implementation of {@link OutputWriter} for writing to a CSV file.
 *
 * @author Aaron Smith
 */
public class SiteBasedCsvFileOutputWriter extends AbstractCsvFileOutputWriter {

    public SiteBasedCsvFileOutputWriter(File outputFile,
                                        List<ExtractionSite> extractionSiteList) throws IOException {
        super(outputFile);

        // Package the dataset for easy lookup.
        for (ExtractionSite extractionSite : extractionSiteList) {
            this.idToDetailsMap.put(
                extractionSite.getId(),
                new String[]{
                    extractionSite.getId(),
                    extractionSite.getSiteName(),
                    Double.toString(extractionSite.getLatitude()),
                    Double.toString(extractionSite.getLongitude())
                }
            );
        }

        // Add the header record.
        this.fileWriter.write(
            "\"Aggregated Date/Time\"," +
                "\"Variable\"," +
                "\"Depth\"," +
                "\"Site Name\"," +
                "\"Latitude\"," +
                "\"Longitude\"," +
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
    public void write(String dateTimeStamp,
                      NcAggregateProductDefinition.SummaryOperator summaryOperator,
                      double depth,
                      String siteId,
                      SummaryStatistics summaryStatistics) throws IOException {

        // Identify the variableName to output.
        final String variableName =
            summaryOperator.getOutputVariables().get(0).getAttributes().get("short_name");

        // Retrieve the lookup record for the zone/site.
        final String[] record = this.idToDetailsMap.get(siteId);
        this.fileWriter.write(
            dateTimeStamp + "," +
                "\"" + variableName + "\"," +
                "\"" + depth + "\"," +
                "\"" + record[1] + "\"," +
                "\"" + record[2] + "\"," +
                "\"" + record[3] + "\"," +
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
