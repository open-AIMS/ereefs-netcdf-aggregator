package aims.ereefs.netcdf.output.netcdf;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.metadata.Metadata;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.metadata.NetCDFMetadata;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Attribute;
import ucar.unidata.util.Parameter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * Utility class for populating the global attributes of a Netcdf Output file. The global attributes
 * are discovered from the following sources:
 *
 * <ul>
 *     <li>Input datasets - where reference datasets will be retrieved from the first TimeInstant.</li>
 *     <li>Calculated attributes - based on the action being performed by this application.</li>
 *     <li>Static attributes - defined in the Product Definition. NOTE that static attributes will
 *     override the previous categories.</li>
 * </ul>
 * <p>
 * The complete list of global attributes will be populated in the order above, so later sources
 * will override earlier sources.
 *
 * @author Aaron Smith
 */
public class GlobalAttributesPopulator {

    static protected Logger logger = LoggerFactory.getLogger(GlobalAttributesPopulator.class);

    // Date formatter.
    final static protected DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    static public void populate(OutputDataset outputDataset,
                                NcAggregateTask task,
                                NcAggregateProductDefinition productDefinition,
                                Map<String, List<String>> datasetMetadataIdsByInputIdMap,
                                MetadataDao metadataDao,
                                AggregationPeriods aggregationPeriod) {

        // The temporary cache for building the list of global attributes.
        final Map<String, String> globalAttributes = new TreeMap<>();

        // One of the global attributes to set is a list of all input datasets used to generate the
        // output dataset. This is formatted as a list of metadataId of the input dataset and the
        // corresponding checksum of the matching input file.
        final Map<String, String> metadataIdToChecksum = new HashMap<>();

        // Loop through each input type.
        for (final String inputId : datasetMetadataIdsByInputIdMap.keySet()) {

            // Loop through each dataset for the input type.
            for (final String metadataId : datasetMetadataIdsByInputIdMap.get(inputId)) {
                final Metadata metadata = metadataDao.getById(metadataId);

                // Add the dataset to the list if it represents a NetCDF file.
                if (metadata instanceof NetCDFMetadata) {
                    metadataIdToChecksum.put(metadataId, ((NetCDFMetadata) metadata).getChecksum());
                }
            }
        }
        globalAttributes.put(
            "aims_ncaggregate_inputs",
            metadataIdToChecksum.entrySet().stream()
                .map(entry -> entry.getKey() + "::" + entry.getValue())
                .collect(Collectors.joining(",", "[", "]"))
        );


        // Include global attributes from a reference dataset for each Input Source. The first
        // dataset will be treated as the reference dataset.

        // Loop through each input type.
        for (final String inputId : datasetMetadataIdsByInputIdMap.keySet()) {

            // Only process if there are input datasets for the input type. Note that this is
            // normally expected to be the case.
            final List<String> datasetMetadataIds = datasetMetadataIdsByInputIdMap.get(inputId);
            if (datasetMetadataIds.size() > 0) {

                // Use the first dataset for the input type.
                final String datasetMetadataId = datasetMetadataIds.get(0);
                final Metadata metadata = metadataDao.getById(datasetMetadataId);
                if (metadata instanceof NetCDFMetadata) {
                    final NetCDFMetadata referenceDatasetMetadata = (NetCDFMetadata) metadata;

                    // Build the list of global attributes.
                    for (final String attributeName : referenceDatasetMetadata.getAttributes().keySet()) {

                        // Ignore "special" attributes as they do not get propagated.
                        if (!attributeName.equalsIgnoreCase("_NCProperties") &&
                            !attributeName.startsWith("aims")) {

                            // Ignore undefined (null) attribute values.
                            String value = referenceDatasetMetadata.getAttributes().get(attributeName);
                            if (value != null) {
                                globalAttributes.put(attributeName, value);
                            }
                        }
                    }
                }
            }
        }

        // Include calculated global attributes.
        final List<Double> aggregatedTimes = task.getTimeInstants().stream()
            .map(timeInstant -> timeInstant.getValue())
            .collect(Collectors.toList());
        logger.debug("aggregatedTimes: " + task.getTimeInstants().size() + " - " + aggregatedTimes.size());
        final LocalDateTime firstDate = DateTimeUtils.toDateTime(aggregatedTimes.get(0));
        globalAttributes.put(
            "aims_ncaggregate_firstDate",
            DATE_TIME_FORMATTER.format(
                firstDate.atZone(
                    ZoneId.of(
                        productDefinition.getTargetTimeZone()
                    )
                )
            )
        );
        final LocalDateTime lastDate = DateTimeUtils.toDateTime(aggregatedTimes.get(aggregatedTimes.size() - 1));
        globalAttributes.put(
            "aims_ncaggregate_lastDate",
            DATE_TIME_FORMATTER.format(
                lastDate.atZone(
                    ZoneId.of(
                        productDefinition.getTargetTimeZone()
                    )
                )
            )
        );

        // Include static attributes.
        if (productDefinition.getOutputs().getNetcdfOutputFile().getGlobalAttributes() != null &&
            productDefinition.getOutputs().getNetcdfOutputFile().getGlobalAttributes().size() > 0) {
            globalAttributes.putAll(productDefinition.getOutputs().getNetcdfOutputFile().getGlobalAttributes());
        }

        // Include datasetId.
        globalAttributes.put("aims_ncaggregate_datasetId", task.getMetadataId());

        // Build date.
        final String buildDate = DATE_TIME_FORMATTER.format(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        globalAttributes.put("aims_ncaggregate_buildDate", buildDate);

        // Update/insert the history attribute.
        String history = globalAttributes.get("history");
        if (history == null) {
            history = "";
        } else {
            history += "\n";
        }
        history += buildDate + ": vendor: AIMS; processing: " + aggregationPeriod.description +
            " summaries";
        globalAttributes.put("history", history);

        // Populate the dataset, ignoring any attributes that are blank or null.
        logger.debug("----- Start -----");
        for (String key : globalAttributes.keySet()) {
            final String value = globalAttributes.get(key);
            if ((value != null) && (value.length() > 0)) {
                logger.debug(key + ": " + value);
                outputDataset.addGroupAttribute(
                    new Attribute(
                        new Parameter(
                            key,
                            value
                        )
                    )
                );
            }
        }
        logger.debug("----- End -----");

    }

}
