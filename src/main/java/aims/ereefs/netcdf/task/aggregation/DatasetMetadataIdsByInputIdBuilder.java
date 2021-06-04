package aims.ereefs.netcdf.task.aggregation;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code Builder} builds a list of the {@code MetadataId} for every input dataset defined in the
 * {@link Task}, and maps them to the {@code id} of the corresponding datasource.
 * <p>
 * Each Input Source will provide one or more datasets as input to the application, and each of
 * those datasets will be represented by a unique MetadataId. A MetadataId is required to retrieve
 * the actual dataset file when ready to process. This class builds a map that binds the unique Id
 * of the Input Source (a product or download source) to the list of unique MetadataIds for each
 * dataset provided by the Input Source. The list of possible input datasets is provided in the
 * {@link Task}.
 *
 * @author Aaron Smith
 */
public class DatasetMetadataIdsByInputIdBuilder {

    static protected Logger logger = LoggerFactory.getLogger(DatasetMetadataIdsByInputIdBuilder.class);

    static public Map<String, List<String>> build(NcAggregateTask task,
                                                  NcAggregateProductDefinition productDefinition) {

        final Map<String, List<String>> inputIdToDatasetMetadataIds = new HashMap<>();
        for (final NcAggregateProductDefinition.Input inputDefn : productDefinition.getInputs()) {

            // The Id of the unique input source, either another Product, or a Download source.
            final String inputId = inputDefn.getId();
            inputIdToDatasetMetadataIds.put(inputId, new ArrayList<>());
            final List<String> metadataIds = inputIdToDatasetMetadataIds.get(inputId);

            // Loop through all TimeInstants for the Task, as each TimeInstant contains a list of
            // MetadataIds that provide data for that TimeInstant.
            for (NcAggregateTask.TimeInstant timeInstant : task.getTimeInstants()) {

                // Within each TimeInstant, dataset MetadataIds are grouped together by input
                // source, so find the entry that matches the input source we are currently
                // processing (as identified by "inputId").
                for (final NcAggregateTask.Input input : timeInstant.getInputs()) {
                    if (input.getInputId().equalsIgnoreCase(inputId)) {

                        // Loop through each dataset MetadataId for the TimeInstant/InputSource
                        // combination, adding any that are missing. This is because a single
                        // input dataset could contain data for multiple TimeInstants, so it might
                        // already be added.
                        for (final NcAggregateTask.FileIndexBounds fileIndexBounds : input.getFileIndexBounds()) {
                            final String metadataId = fileIndexBounds.getMetadataId();
                            if (!metadataIds.contains(metadataId)) {
                                metadataIds.add(metadataId);
                            }
                        }
                    }
                }
            }
        }

        return inputIdToDatasetMetadataIds;

    }

}
