package aims.ereefs.netcdf;

import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinitionDao;
import au.gov.aims.ereefs.pojo.extractionrequest.ExtractionRequestDao;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.task.Task;
import au.gov.aims.ereefs.pojo.task.TaskDao;
import com.mongodb.client.MongoClient;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A global context for immutable application-wide settings and references, mostly populated at
 * application start up. Once a property is set, attempting to modify the property will result in
 * a {@code RuntimeException}.
 *
 * @author Aaron Smith
 */
public class ApplicationContext {

    protected String executionEnvironment;

    public ApplicationContext(String executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    // ---------------------------------------------------------------------------------------------
    // Task
    // ---------------------------------------------------------------------------------------------

    /**
     * Cached reference to the {@link NcAggregateTask} being processed.
     */
    protected NcAggregateTask task = null;

    public NcAggregateTask getTask() {
        return this.task;
    }

    public void setTask(NcAggregateTask task) {
        if (this.task == null) {
            this.task = task;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ProductDefinition
    // ---------------------------------------------------------------------------------------------

    /**
     * Cached reference to the {@link ProductDefinition} for the product being generated.
     */
    protected NcAggregateProductDefinition productDefinition = null;

    public ProductDefinition getProductDefinition() {
        return this.productDefinition;
    }

    public void setProductDefinition(NcAggregateProductDefinition productDefinition) {
        if (this.productDefinition == null) {
            this.productDefinition = productDefinition;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Inputs
    // ---------------------------------------------------------------------------------------------

    /**
     * Look up for retrieving the corresponding
     * {@link NcAggregateProductDefinition.Input} definition from a specific variables
     * fully qualified name.
     */
    protected Map<String, NcAggregateProductDefinition.Input> inputDefinitionByVariableNameMap = null;

    public Map<String, NcAggregateProductDefinition.Input> getInputDefinitionByVariableNameMap() {
        return this.inputDefinitionByVariableNameMap;
    }

    public void setInputDefinitionByVariableNameMap(Map<String, NcAggregateProductDefinition.Input> inputDefinitionByVariableNameMap) {
        if (this.inputDefinitionByVariableNameMap == null) {
            this.inputDefinitionByVariableNameMap = inputDefinitionByVariableNameMap;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    /**
     * Cached reference to the {@link InputDatasetCache} for application-wide download and caching
     * of input datasets.
     */
    protected InputDatasetCache inputDatasetCache = null;

    public InputDatasetCache getInputDatasetCache() {
        return this.inputDatasetCache;
    }

    public void setInputDatasetCache(InputDatasetCache inputDatasetCache) {
        if (this.inputDatasetCache == null) {
            this.inputDatasetCache = inputDatasetCache;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    /**
     * A list of MetadataIds for each InputSourceId.
     */
    protected Map<String, List<String>> datasetMetadataIdsByInputIdMap = null;

    public Map<String, List<String>> getDatasetMetadataIdsByInputIdMap() {
        return this.datasetMetadataIdsByInputIdMap;
    }

    public void setDatasetMetadataIdsByInputIdMap(Map<String, List<String>> datasetMetadataIdsByInputIdMap) {
        if (this.datasetMetadataIdsByInputIdMap == null) {
            this.datasetMetadataIdsByInputIdMap = datasetMetadataIdsByInputIdMap;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Generic Cache
    // ---------------------------------------------------------------------------------------------

    /**
     * Generic cache which references can be bound to.
     */
    protected Map<String, Object> cache = new TreeMap<>();

    public void putInCache(String key, Object object) {
        if (this.cache.containsKey(key)) {
            throw new RuntimeException("The key \"" + key + "\" already exists in cache.");
        } else {
            this.cache.put(key, object);
        }
    }

    public Object getFromCache(String key) {
        return this.cache.get(key);
    }


    // ---------------------------------------------------------------------------------------------
    // Outputs
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    // Action
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    // DAOs
    // ---------------------------------------------------------------------------------------------

    /**
     * Cached reference to the {@code MongoDatabase} connection so it can be closed when the
     * application terminates.
     */
    protected MongoClient mongoClient = null;

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Cached reference to the {@code Dao} for accessing
     * {@link au.gov.aims.ereefs.pojo.metadata.Metadata}s in the database.
     */
    protected MetadataDao metadataDao = null;

    public MetadataDao getMetadataDao() {
        return this.metadataDao;
    }

    public void setMetadataDao(MetadataDao dao) {
        if (this.metadataDao == null) {
            this.metadataDao = dao;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    /**
     * Cached reference to the {@code Dao} for accessing {@link ProductDefinition}s in the database.
     */
    protected ProductDefinitionDao productDefinitionDao = null;

    public ProductDefinitionDao getProductDefinitionDao() {
        return this.productDefinitionDao;
    }

    public void setProductDefinitionDao(ProductDefinitionDao dao) {
        if (this.productDefinitionDao == null) {
            this.productDefinitionDao = dao;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    /**
     * Cached reference to the {@code Dao} for accessing {@code ExtractionRequest} objects in the database.
     */
    protected ExtractionRequestDao extractionRequestDao = null;

    public ExtractionRequestDao getExtractionRequestDao() {
        return this.extractionRequestDao;
    }

    public void setExtractionRequestDao(ExtractionRequestDao extractionRequestDao) {
        this.extractionRequestDao = extractionRequestDao;
    }

    /**
     * Cached reference to the {@code Dao} for accessing {@link Task}s in the database.
     */
    protected TaskDao taskDao = null;

    public TaskDao getTaskDao() {
        return this.taskDao;
    }

    public void setTaskDao(TaskDao dao) {
        if (this.taskDao == null) {
            this.taskDao = dao;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

    /**
     * The path name of the temp directory for use within the application.
     */
    protected String tempPathname = null;

    public String getTempPathname() {
        return tempPathname;
    }

    public void setTempPathname(String pathName) {
        if (this.tempPathname == null) {
            this.tempPathname = pathName;
        } else {
            throw new RuntimeException("Immutable properties may not be modified.");
        }
    }

}
