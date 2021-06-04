package aims.ereefs.netcdf.task.aggregation;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@code Builder} that creates a map of {@code InputId} to the corresponding input source
 * definition for convenience.
 *
 * @author Aaron Smith
 */
public class InputIdToInputDefinitionMapBuilder {

    static protected Logger logger = LoggerFactory.getLogger(InputIdToInputDefinitionMapBuilder.class);

    static public Map<String, NcAggregateProductDefinition.Input> build(
        NcAggregateProductDefinition productDefinition) {
        final Map<String, NcAggregateProductDefinition.Input> map = new TreeMap<>();
        for (NcAggregateProductDefinition.Input input : productDefinition.getInputs()) {
            map.put(input.getId(), input);
        }
        return map;
    }
}
