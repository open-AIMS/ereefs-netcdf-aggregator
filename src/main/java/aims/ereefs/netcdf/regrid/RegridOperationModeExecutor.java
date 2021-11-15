package aims.ereefs.netcdf.regrid;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.ApplicationContextBuilder;
import aims.ereefs.netcdf.OperationModeExecutor;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.input.netcdf.InputDatasetBuilder;
import aims.ereefs.netcdf.task.aggregation.AggregationTaskExecutor;
import aims.ereefs.netcdf.util.TempDirectoryInitialiser;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.metadata.MetadataDaoFileImpl;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.task.TaskDao;
import au.gov.aims.ereefs.pojo.task.TaskDaoFileImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateUnit;
import ucar.nc2.util.CancelTask;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link OperationModeExecutor} implementation for regridding NetCDF files from the command-line.
 *
 * @author Aaron Smith
 */
public class RegridOperationModeExecutor implements OperationModeExecutor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constant identifying the default value for {@code resolution}.
     */
    protected final double DEFAULT_RESOLUTION = 0.03;

    /**
     * Return {@code true} if {@code args} contains {@code --regrid}.
     */
    @Override
    public boolean supports(String[] args) {
        return Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("--regrid"));
    }

    /**
     * Coordinate the {@code Regridding} operation.
     */
    @Override
    public void execute(String[] args) {
        logger.debug("Executing");

        // Declare constants that are used throughout the method.
        final String PRODUCT_DEFINITION_ID = "regriddedNetCDF";

        // Capture the command line arguments.
        String inputPath = null;
        String outputPath = null;
        String cacheFilename = null;
        String[] specifiedVariableNames = new String[0];
        double tempResolution = DEFAULT_RESOLUTION;
        for (final String arg : args) {

            if (arg.startsWith("--input=")) {
                inputPath = arg.substring("--input=".length());
            }

            if (arg.startsWith("--output=")) {
                outputPath = arg.substring("--output=".length());
            }

            if (arg.startsWith("--cache=")) {
                cacheFilename = arg.substring("--cache=".length());
            }

            if (arg.startsWith("--resolution=")) {
                tempResolution = Double.parseDouble(arg.substring("--resolution=".length()));
            }

            if (arg.startsWith("--variables=")) {
                specifiedVariableNames = arg.substring("--variables=".length()).split(",");
            }

        }
        if (inputPath == null) {
            throw new RuntimeException("Input path not specified. Use \"--input=<input path>\"");
        }
        logger.debug("\"inputPath\" : " + inputPath);
        final String INPUT_ID = inputPath.endsWith(File.separator) ?
                inputPath :
                inputPath + File.separator;
        if (outputPath == null) {
            throw new RuntimeException("Output path not specified. Use \"--output=<output path>\"");
        }
        if (!outputPath.endsWith(File.separator)) {
            outputPath = outputPath + File.separator;
        }
        logger.debug("\"outputPath\" : " + outputPath);
        if (cacheFilename == null) {
            logger.debug("\"cache\" not specified. You can use \"--cache=<cache filename>\"");
        } else {
            logger.debug("\"cache\" : " + cacheFilename);
        }
        final double resolution = tempResolution;
        logger.debug("\"resolution\" : " + resolution);
        if (specifiedVariableNames.length > 0) {
            logger.debug("variables: " +
                Arrays.stream(specifiedVariableNames).collect(Collectors.joining(",")));
        }

        // Identify the files to process.
        File path = new File(inputPath);
        if (!path.exists()) {
            throw new RuntimeException("\"" + inputPath + "\" does not exist.");
        }
        File[] files = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().endsWith(".NC");
            }
        });
        logger.debug("\"" + inputPath + "\" has " + files.length + " files.");

        // Instantiate a MetadataDao which will be progressively populated with the metadata of the
        // files being processed. Note that we use a file-based implementation because ncAggregate
        // is running stand-alone.
        String tempDir = TempDirectoryInitialiser.initialise();
        final String daoPath = tempDir + "dao" + File.separator;
        final MetadataDao metadataDao = new MetadataDaoFileImpl(daoPath + "metadata");
        final TaskDao taskDao = new TaskDaoFileImpl(daoPath + "task");

        // Process each file.
        for (File file : files) {
            logger.debug("Regridding file: " + file.getAbsolutePath());
            final String outputFilename = "file:" + outputPath + "regridded-" + file.getName();

            // Open the NetCDF dataset.
            NetcdfDataset.disableNetcdfFileCache();
            NetcdfDataset dataset = null;
            try {
                dataset = NetcdfDataset.openDataset(

                    // location
                    file.getAbsolutePath(),

                    // enhance
                    true,

                    // cancelTask
                    (CancelTask) null
                );
                NetcdfDataset.disableNetcdfFileCache();
            } catch (IOException e) {
                String msg = "Failed to open the dataset \"" + file.getName() + "\".";
                this.logger.error(msg, e);
                throw new RuntimeException(msg, e);
            }
            final DateUnit dateUnit = NetcdfDateUtils.getDateUnit(dataset);

            // Wrap the NetCDF dataset as an InputDataset for additional functionality.
            final InputDataset inputDataset = InputDatasetBuilder.build(
                dataset,
                new ArrayList<Double>(),
                false
            );

            // Generate the Metadata for the file and persist it to the database.
            NetCDFMetadataBean netcdfMetadataBean = null;
            try {
                netcdfMetadataBean = NetCDFMetadataBean.create(
                    "input",
                    file.getName(),
                    new URI("file:" + file.getAbsolutePath()),
                    new File(file.getAbsolutePath()),
                    DateTime.now().getMillis(),
                    true
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                    "Failed to build URI of input file \"" + file.getName() + "\".", e);
            }
            final String metadataId = netcdfMetadataBean.getId();
            logger.debug("metadataId: " + metadataId);
            metadataDao.persist(netcdfMetadataBean.toJSON());

            // Find the variable with the most time values. These will be used as the time values
            // for the file.
            VariableMetadataBean referenceVariable = null;
            int referenceVariableTimeValuesSize = 0;
            for (final String variableName : netcdfMetadataBean.getVariableMetadataBeanMap().keySet()) {
                VariableMetadataBean variable = netcdfMetadataBean.getVariableMetadataBeanMap().get(variableName);
                int timeValuesSize = 0;
                if (variable.getTemporalDomainBean() != null) {
                    timeValuesSize = variable.getTemporalDomainBean().getTimeValues().size();
                }
                if (variable.getTemporalDomainBean() != null) {
                    if (referenceVariable == null || timeValuesSize > referenceVariableTimeValuesSize) {
                        referenceVariable = variable;
                        referenceVariableTimeValuesSize = referenceVariable.getTemporalDomainBean().getTimeValues().size();
                    }
                }
            }
            if (referenceVariable == null) {
                throw new RuntimeException("Unable to find variable with time dimension.");
            }

            final List<DateTime> variableTimeValues = referenceVariable.getTemporalDomainBean().getTimeValues();
            if (variableTimeValues == null || variableTimeValues.size() == 0) {
                throw new RuntimeException("No time values found in \"" + file.getName() + "\".");
            }

            // Build a list of TimeInstant objects which are part of the Task defining the work to
            // be done. Each time instant represents a single value from the time variable.
            List<NcAggregateTask.TimeInstant> timeInstants = new ArrayList<>();
            for (int index = 0; index < variableTimeValues.size(); index++) {
                final DateTime dateTime = variableTimeValues.get(index);
                double timeValue = NetcdfDateUtils.fromLocalDateTime(
                    dateUnit,
                    Instant
                        .ofEpochMilli(dateTime.getMillis())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                );
                int finalIndex = index;
                timeInstants.add(
                    new NcAggregateTask.TimeInstant(
                        timeValue,
                        new ArrayList<NcAggregateTask.Input>() {{
                            add(
                                new NcAggregateTask.Input(
                                    INPUT_ID,
                                    new ArrayList<NcAggregateTask.FileIndexBounds>() {{
                                        add(
                                            new NcAggregateTask.FileIndexBounds(
                                                metadataId,
                                                finalIndex,
                                                finalIndex
                                            )
                                        );
                                    }}
                                )
                            );
                        }}
                    )
                );
            }

            // Determine the timeIncrement unit. Default is "daily". This is used when populating
            // the ProductDefinition we will build shortly.
            String timeIncrement = "daily";
            if (variableTimeValues.size() > 1) {
                final long timeDiff = variableTimeValues.get(1).getMillis() -
                    variableTimeValues.get(0).getMillis();
                if (timeDiff == 60 * 60 * 1000) {
                    timeIncrement = "hourly";
                }
                if (timeDiff == 24 * 60 * 60 * 1000) {
                    timeIncrement = "daily";
                }
            }

            // Determine the file duration. Default is "monthly". This is used when populating the
            // ProductDefinition we will build shortly.
            String fileDuration = "monthly";
            final long timeDiff = variableTimeValues.get(variableTimeValues.size() - 1).getMillis() -
                variableTimeValues.get(0).getMillis();
            if (timeDiff == 24 * 60 * 60 * 1000) {
                fileDuration = "daily";
            }

            // Build the Product definition. Note that the variables referenced are NOT of type
            // CoordinateAxis, which are the four (4) dimensions and will be handled separately.
            String[] finalSpecifiedVariableNames = specifiedVariableNames;
            final String[] variableNames =

                // Start with all variables in the input dataset.
                inputDataset.getVariables()
                    .stream()

                    // Ignore the variables for the dimensions.
                    .filter(variable -> !(variable instanceof CoordinateAxis))

                    // Ensure the variable has a time dimension.
                    .filter(variable -> variable.findDimensionIndex("time") != -1)

                    // Consider only the fullname of the variable.
                    .map(variable -> variable.getFullName())

                    // If variables were specified via command-line, only include those variables,
                    // otherwise include all variables that have not been filtered previously.
                    .filter(variableName -> finalSpecifiedVariableNames.length == 0 ||
                            Arrays.stream(finalSpecifiedVariableNames)
                        .filter(specifiedVariableName -> specifiedVariableName.equalsIgnoreCase(variableName))
                        .findFirst()
                        .isPresent()
                    )
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
            if (variableNames.length == 0) {
                throw new RuntimeException("No variables to process.");
            }
            logger.debug("variables: " +
                Arrays.stream(variableNames).collect(Collectors.joining(",")));
            ObjectMapper objectMapper = new ObjectMapper();
            final List<NcAggregateProductDefinition.PreProcessingTaskDefn> preProcessingTaskDefns =
                new ArrayList();
            try {
                preProcessingTaskDefns.add(
                    new NcAggregateProductDefinition.PreProcessingTaskDefn(
                        objectMapper.readTree(
                            "{" +
                                "\"type\": \"RegularGridMapperCachePopulatorTask\"," +
                                "\"files\": " +
                                "[{" +
                                "\"url\": \"file:" + cacheFilename + "\"," +
                                "\"bindName\": \"regularGridMapper\"," +
                                "\"resolution\": " + resolution +
                                "}]}"
                        )
                    )
                );
            } catch (IOException e) {
                throw new RuntimeException(
                    "Failed to parse JSON for RegularGridMapper definition.", e);
            }
            NcAggregateProductDefinition productDefinition = NcAggregateProductDefinition.make(
                PRODUCT_DEFINITION_ID,
                "Australia/Brisbane",
                new ProductDefinition.Filters(new ProductDefinition.DateRange[0]),
                new NcAggregateProductDefinition.NetCDFInput[]{
                    NcAggregateProductDefinition.NetCDFInput.make(
                        INPUT_ID,
                        "netcdf",
                        timeIncrement,
                        fileDuration,
                        false,
                        new String[0]
                    )
                },
                preProcessingTaskDefns,
                new NcAggregateProductDefinition.Action(
                    "none",
                    new double[0],
                    variableNames,
                    new NcAggregateProductDefinition.SummaryOperator[0]
                ),
                new NcAggregateProductDefinition.Outputs(
                    NcAggregateProductDefinition.OutputsStrategy.DAILY,
                    false,
                    outputPath,
                    new ArrayList<NcAggregateProductDefinition.OutputFile>() {{
                        add(
                            new NcAggregateProductDefinition.NetcdfOutputFile(
                                NcAggregateProductDefinition.OutputFileType.NETCDF,
                                "regularGridMapper",
                                new HashMap<String, String>()
                            )
                        );
                    }}
                )
            );

            NcAggregateTask task = new NcAggregateTask(
                "jobId",
                PRODUCT_DEFINITION_ID,
                "regridded/" + file.getName(),
                outputFilename,
                timeInstants);
            taskDao.persist(task);

            // Build an ApplicationContext which can be passed to the executor for app-level
            // references.
            final ApplicationContext applicationContext = new ApplicationContext("regrid");
            applicationContext.setTask(task);
            applicationContext.setProductDefinition(productDefinition);
            applicationContext.setMetadataDao(metadataDao);
            applicationContext.setTaskDao(taskDao);
            applicationContext.setTempPathname(tempDir);
            ApplicationContextBuilder.populateApplicationContext(
                applicationContext,
                task,
                productDefinition
            );

            // Execute the aggregator on the file. Aggregation will not be performed because of
            // settings we made in the ProductDefinition, but the aggregator will perform regridding
            // on the file.
            AggregationTaskExecutor aggregationTaskExecutor = new AggregationTaskExecutor();
            aggregationTaskExecutor.execute(task, applicationContext);
        }
    }

}
