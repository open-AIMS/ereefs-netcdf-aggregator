package aims.ereefs.netcdf.util;

import au.gov.aims.ereefs.pojo.task.NcAggregateTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating a {@code Task}.
 *
 * @author Aaron Smith
 */
public class NcAggregateTaskGenerator {

    /**
     * Utility method that instantiates the {@link NcAggregateTask}.
     */
    static public NcAggregateTask generate(String productId,
                                           List<NcAggregateTask.TimeInstant> timeInstants) {
        return new NcAggregateTask(
            "jobId",
            productId,
            "outputMetadataId",
            "baseUrl",
            new ArrayList<NcAggregateTask.TimeInstant>() {{
                addAll(timeInstants);
            }}
        );

    }

    static public List<NcAggregateTask.TimeInstant> makeTimeInstants(String inputId,
                                                                     Map<Double, Map<String, Integer>> timeInstantToInputMetadataIdToEndIndexMap) {
        final List<NcAggregateTask.TimeInstant> timeInstants = new ArrayList<>();
        for (double timeInstantValue : timeInstantToInputMetadataIdToEndIndexMap.keySet()) {
            final Map<String, Integer> inputMetadataIdToEndIndexMap = timeInstantToInputMetadataIdToEndIndexMap.get(timeInstantValue);
            final List<NcAggregateTask.FileIndexBounds> fileIndexBounds = new ArrayList<>();
            for (final String metadataId : inputMetadataIdToEndIndexMap.keySet()) {
                int endIndex = inputMetadataIdToEndIndexMap.get(metadataId);
                fileIndexBounds.add(new NcAggregateTask.FileIndexBounds(metadataId, 0, endIndex));
            }
            timeInstants.add(
                new NcAggregateTask.TimeInstant(
                    timeInstantValue,
                    new ArrayList<NcAggregateTask.Input>() {
                        {
                            add(
                                new NcAggregateTask.Input(
                                    inputId,
                                    fileIndexBounds
                                )
                            );
                        }
                    }
                )
            );
        }
        return timeInstants;
    }

}
