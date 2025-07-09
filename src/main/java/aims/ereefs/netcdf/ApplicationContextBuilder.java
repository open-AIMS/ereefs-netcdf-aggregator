package aims.ereefs.netcdf;

import aims.ereefs.netcdf.input.csv.CsvDatasetCachePopulatorTask;
import aims.ereefs.netcdf.input.csv.ThresholdCachePopulatorTask;
import aims.ereefs.netcdf.input.extraction.ExtractionSitesBuilderTask;
import aims.ereefs.netcdf.input.geojson.GeoJsonCachePopulatorTask;
import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import aims.ereefs.netcdf.input.netcdf.InputDatasetCacheFactory;
import aims.ereefs.netcdf.regrid.RegularGridMapperCachePopulatorTask;
import aims.ereefs.netcdf.task.aggregation.DatasetMetadataIdsByInputIdBuilder;
import aims.ereefs.netcdf.task.aggregation.InputDefinitionByVariableNameMapBuilder;
import aims.ereefs.netcdf.task.aggregation.InputIdToInputDefinitionMapBuilder;
import aims.ereefs.netcdf.tasks.PreProcessingTask;
import aims.ereefs.netcdf.util.EnvironmentVariableReader;
import aims.ereefs.netcdf.util.ParameterStoreReader;
import aims.ereefs.netcdf.util.TempDirectoryInitialiser;
import aims.ereefs.netcdf.util.file.cache.FileCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinitionDaoFileImpl;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinitionDaoMongoDbImpl;
import au.gov.aims.ereefs.pojo.extractionrequest.ExtractionRequest;
import au.gov.aims.ereefs.pojo.extractionrequest.ExtractionRequestDaoFileImpl;
import au.gov.aims.ereefs.pojo.extractionrequest.ExtractionRequestDaoMongoDbImpl;
import au.gov.aims.ereefs.pojo.extractionrequest.TransformUtils;
import au.gov.aims.ereefs.pojo.metadata.MetadataDaoFileImpl;
import au.gov.aims.ereefs.pojo.metadata.MetadataDaoMongoDbImpl;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.task.Task;
import au.gov.aims.ereefs.pojo.task.TaskDaoFileImpl;
import au.gov.aims.ereefs.pojo.task.TaskDaoMongoDbImpl;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Builder class responsible for instantiating an {@link ApplicationContext} and any references it
 * will hold.
 *
 * @author Aaron Smith
 */
public class ApplicationContextBuilder {

    static protected Logger logger = LoggerFactory.getLogger(ApplicationContextBuilder.class);

    static final protected int DEFAULT_MAX_FILE_CACHE_SIZE_GB = 40;

    private static final int MONGODB_TIMEOUT = 300; // 5 minutes

    /**
     * Build the {@link ApplicationContext} from environment parameters.
     */
    static public ApplicationContext build() {

        // Identify the execution environment (eg: "PROD", "TEST", "asmith").
        final String executionEnvironment = EnvironmentVariableReader.getInstance().getByKey("EXECUTION_ENVIRONMENT");
        ParameterStoreReader.setExecutionEnvironment(executionEnvironment);

        // Instantiate the ApplicationContext to be populated.
        final ApplicationContext applicationContext = new ApplicationContext(executionEnvironment);

        final String taskId = EnvironmentVariableReader.getInstance().getByKey("TASK_ID");
        logger.info("taskId: " + taskId);

        // Instantiate the DAO layer.
        populateDao(applicationContext);

        // Populate the Task.
        final Task tempTask = applicationContext.getTaskDao().getById(taskId);
        if (tempTask == null) {
            throw new RuntimeException("Task \"" + taskId + "\" could not be found.");
        }
        if (!(tempTask instanceof NcAggregateTask)) {
            throw new RuntimeException("Unexpected Task type (\"" + tempTask.getClass().getSimpleName() + "\").");
        }
        NcAggregateTask task = (NcAggregateTask) tempTask;
        applicationContext.setTask(task);

        // Attempt to retrieve the ProductDefinition from the database. This could be an actual
        // ProductDefinition, or it could be a Data Extraction Request. In the case of the latter,
        // it must be transformed before it can be used.
        ProductDefinition tempProductDefinition =
            applicationContext.getProductDefinitionDao().getById(task.getProductDefinitionId());
        if (tempProductDefinition == null) {
            // Attempt to retrieve and transform a Data Extraction Request.
            ExtractionRequest extractionRequest =
                applicationContext.getExtractionRequestDao().getById(task.getProductDefinitionId());
            if (extractionRequest != null) {
                tempProductDefinition = TransformUtils.transform(extractionRequest);
            }
        }
        if (tempProductDefinition == null) {
            throw new RuntimeException("Unknown product (\"" + task.getProductDefinitionId() + "\").");
        }
        if (!(tempProductDefinition instanceof NcAggregateProductDefinition)) {
            throw new RuntimeException("Unsupported product (\"" + tempProductDefinition.getClass().getName() + "\").");
        }
        final NcAggregateProductDefinition productDefinition =
            (NcAggregateProductDefinition) tempProductDefinition;
        logger.debug("ProductId: " + productDefinition.getId());
        logger.debug("MetadataId: " + task.getMetadataId());
        applicationContext.setProductDefinition(productDefinition);

        // Populate the ApplicationContext with other generic references.
        populateApplicationContext(applicationContext, task, productDefinition);

        return applicationContext;
    }

    /**
     * Populate the {@link ApplicationContext} with generic references.
     */
    static public void populateApplicationContext(ApplicationContext applicationContext,
                                                  NcAggregateTask task,
                                                  NcAggregateProductDefinition productDefinition) {

        // Set Temp directory if not already set. This allows Temp directory to be set in other
        // use cases as a work around. Not great, but more elegant than alternatives.
        if (applicationContext.getTempPathname() == null) {
            applicationContext.setTempPathname(TempDirectoryInitialiser.initialise());
        }
        final String tempPathname = applicationContext.getTempPathname();

        // Central download file cache. This provides seamless download of files on demand, and
        // automated disk space control (ie: delete files when space is required).
        final String downloadPathname = tempPathname + "downloads" + File.separator;
        final String maxFileCacheSizeStr = EnvironmentVariableReader.getInstance().optByKey("MAX_FILE_CACHE_SIZE_GB");
        final int maxFileCacheSize = maxFileCacheSizeStr != null ?
            Integer.parseInt(maxFileCacheSizeStr) : DEFAULT_MAX_FILE_CACHE_SIZE_GB;
        final FileCache fileCache = new FileCache(downloadPathname, maxFileCacheSize);

        // Build a list of MetadataIds for each InputId.
        applicationContext.setDatasetMetadataIdsByInputIdMap(
            DatasetMetadataIdsByInputIdBuilder.build(task, productDefinition)
        );

        // Provide seamless access to input datasets. The InputDatasetCache uses the FileCache to
        // download datasets on demand.
        final InputDatasetCache inputDatasetCache = InputDatasetCacheFactory.make(
            fileCache,
            applicationContext.getMetadataDao(),
            productDefinition,
            applicationContext.getDatasetMetadataIdsByInputIdMap()
        );
        applicationContext.setInputDatasetCache(inputDatasetCache);

        // Force the InputDatasetCache to load a reference dataset, because other classes that
        // require a reference dataset do not have access to any of the MetadataIds to determine
        // the reference datasets.

        // Pre Processing Tasks.
        List<PreProcessingTask> supportedPreProcessingTasks = new ArrayList<PreProcessingTask>() {{
            add(new CsvDatasetCachePopulatorTask(tempPathname));
            add(new ExtractionSitesBuilderTask(tempPathname));
            add(new GeoJsonCachePopulatorTask(tempPathname));
            add(new RegularGridMapperCachePopulatorTask(tempPathname));
            add(new ThresholdCachePopulatorTask(tempPathname));
        }};
        for (final NcAggregateProductDefinition.PreProcessingTaskDefn preProcessingTaskDefn : productDefinition.getPreProcessingTasks()) {
            boolean isHandled = false;
            for (PreProcessingTask preProcessingTaskImpl : supportedPreProcessingTasks) {
                if (preProcessingTaskImpl.supports(preProcessingTaskDefn)) {
                    isHandled = true;
                    preProcessingTaskImpl.process(preProcessingTaskDefn, applicationContext);
                }
            }
            if (!isHandled) {
                throw new RuntimeException("\"" + preProcessingTaskDefn.getType() + "\" not supported.");
            }
        }

        // A ProductDefinition supports multiple Input Sources, which are defined as an array.
        // Build a Map for easier access.
        // TODO: Is this needed? Could use a util that performs the search for reference on demand.
        final Map<String, NcAggregateProductDefinition.Input> inputDefinitionByInputIdMap =
            InputIdToInputDefinitionMapBuilder.build(productDefinition);

        // Build the lookup map to retrieve the corresponding InputDefinition based on the fully
        // qualified variable name.
        applicationContext.setInputDefinitionByVariableNameMap(
            InputDefinitionByVariableNameMapBuilder.build(
                productDefinition,
                task,
                inputDatasetCache,
                inputDefinitionByInputIdMap
            )
        );

    }

    /**
     * Helper method to populate the {@link ApplicationContext} with references to the {@code DAO}
     * objects.
     */
    static public void populateDao(ApplicationContext applicationContext) {
        final String dbType = EnvironmentVariableReader.getInstance().optByKey("DB_TYPE");
        if ("file".equalsIgnoreCase(dbType)) {
            populateFileDao(applicationContext);
        } else {
            populateMongoDao(applicationContext);
        }
    }

    /**
     * Instantiate a file-based DAO layer.
     */
    static protected void populateFileDao(ApplicationContext applicationContext) {
        logger.debug("Using file-based repositories.");

        final String DB_PATH = "DB_PATH";
        String filePath = EnvironmentVariableReader.getInstance().getByKey(DB_PATH);
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }

        // Instantiate the DAOs.
        applicationContext.setTaskDao(new TaskDaoFileImpl(filePath + "tasks"));
        applicationContext.setProductDefinitionDao(
            new ProductDefinitionDaoFileImpl(
                filePath + "definitions" + File.separator + "products" + File.separator
            )
        );
        applicationContext.setExtractionRequestDao(
            new ExtractionRequestDaoFileImpl(
                filePath + "definitions" + File.separator + "extraction_request" + File.separator
            )
        );
        applicationContext.setMetadataDao(
            new MetadataDaoFileImpl(
                filePath + "metadata" + File.separator
            )
        );

    }

    /**
     * Instantiate a MongoDB-based DAO layer.
     */
    static protected void populateMongoDao(ApplicationContext applicationContext) {
        logger.debug("Using MongoDB-based repositories.");

        String host = EnvironmentVariableReader.getInstance().optByKey("MONGODB_HOST");
        if (host == null) {
            host = ParameterStoreReader.getByKey("/global/mongodb/host");
        }
        String port = EnvironmentVariableReader.getInstance().optByKey("MONGODB_PORT");
        if (port == null) {
            port = ParameterStoreReader.getByKey("/global/mongodb/port");
        }
        String db = EnvironmentVariableReader.getInstance().optByKey("MONGODB_DB");
        if (db == null) {
            db = ParameterStoreReader.getByKey("/global/mongodb/db");
        }
        String userId = EnvironmentVariableReader.getInstance().optByKey("MONGODB_USER_ID");
        if (userId == null) {
            userId = ParameterStoreReader.getByKey("/ncAggregate/mongodb/userid");
        }
        String password = EnvironmentVariableReader.getInstance().optByKey("MONGODB_PASSWORD");
        if (password == null) {
            password = ParameterStoreReader.getByKey("/ncAggregate/mongodb/password");
        }

        // Connect to the MongoDB instance.
        MongoClient mongoClient;
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://" + userId + ":" +
                        URLEncoder.encode(password, "UTF-8") + "@" + host + ":" + port))
                .retryWrites(true)
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(MONGODB_TIMEOUT, TimeUnit.SECONDS)
                                .readTimeout(MONGODB_TIMEOUT, TimeUnit.SECONDS))
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(MONGODB_TIMEOUT, TimeUnit.SECONDS))
                .build();

            mongoClient = MongoClients.create(settings);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to connect to MongoDB.", e);
        }

        applicationContext.setMongoClient(mongoClient);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(db);

        applicationContext.setTaskDao(new TaskDaoMongoDbImpl(mongoDatabase));
        applicationContext.setProductDefinitionDao(
            new ProductDefinitionDaoMongoDbImpl(mongoDatabase)
        );
        applicationContext.setExtractionRequestDao(
            new ExtractionRequestDaoMongoDbImpl(mongoDatabase)
        );
        applicationContext.setMetadataDao(new MetadataDaoMongoDbImpl(mongoDatabase));
    }

}