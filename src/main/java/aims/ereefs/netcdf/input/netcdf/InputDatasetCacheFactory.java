package aims.ereefs.netcdf.input.netcdf;

import aims.ereefs.netcdf.util.file.cache.FileCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * {@code Factory} for instantiating a {@link InputDatasetCache} for application-wide
 * download and caching of input datasets.
 *
 * @author Aaron Smith
 */
public class InputDatasetCacheFactory {

    static protected Logger logger = LoggerFactory.getLogger(InputDatasetCacheFactory.class);

    static public InputDatasetCache make(FileCache fileCache,
                                         MetadataDao metadataDao,
                                         NcAggregateProductDefinition productDefinition,
                                         Map<String, List<String>> datasetMetadataIdsByInputIdMap) {

        return new InputDatasetCache(
            fileCache,
            metadataDao,
            productDefinition.getAction().getDepths(),
            datasetMetadataIdsByInputIdMap
        );
    }
}