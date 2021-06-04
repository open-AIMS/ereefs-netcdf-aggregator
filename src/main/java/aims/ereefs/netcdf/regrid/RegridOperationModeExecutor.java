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
 * {@link OperationModeExecutor} implementation for regridding.
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
        String[] specifiedVariableNames = null;
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
        final String INPUT_ID = inputPath;
        if (outputPath == null) {
            throw new RuntimeException("Output path not specified. Use \"--output=<output path>\"");
        }
        logger.debug("\"outputPath\" : " + outputPath);
        if (cacheFilename == null) {
            logger.debug("\"cache\" not specified. You can use \"--cache=<cache filename>\"");
        } else {
            logger.debug("\"cache\" : " + cacheFilename);
        }
        final double resolution = tempResolution;
        logger.debug("\"resolution\" : " + resolution);
        if (specifiedVariableNames != null) {
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
        // files being processed.
        String tempDir = TempDirectoryInitialiser.initialise();
        final MetadataDao metadataDao = new MetadataDaoFileImpl(tempDir);

        // Process each file.
        for (File file : files) {
            logger.debug(file.getAbsolutePath());
            final String DATASET_ID = file.getName();
            final String METADATA_ID = PRODUCT_DEFINITION_ID + "/" + DATASET_ID;
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

            // Generate the Metadata for the file.
            NetCDFMetadataBean netcdfMetadataBean = null;
            try {
                netcdfMetadataBean = NetCDFMetadataBean.create(
                    PRODUCT_DEFINITION_ID,
                    DATASET_ID,
                    new URI("file:" + file.getAbsolutePath()),
                    new File(file.getAbsolutePath()),
                    DateTime.now().getMillis(),
                    false
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                    "Failed to build URI of input file \"" + file.getName() + "\".", e);
            }
            logger.debug("metadataId: " + netcdfMetadataBean.getId());
            metadataDao.persist(netcdfMetadataBean.toJSON());

            // Find the variable with the most time values.
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
                                                METADATA_ID,
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

            // Calculate the timeIncrement value.
            String timeIncrement = null;
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
            if (timeIncrement == null) {
                throw new RuntimeException("Unable to calculate the time increments for the file.");
            }

            // Calculate the file duration value.
            String fileDuration = "monthly";
            final long timeDiff = variableTimeValues.get(variableTimeValues.size() - 1).getMillis() -
                variableTimeValues.get(0).getMillis();
            if (timeDiff == 24 * 60 * 60 * 1000) {
                fileDuration = "daily";
            }

            // Build the Product definition. Note that the variables referenced are NOT of type
            // CoordinateAxis, which are the four (4) dimensions. They will be handled separately.
            String[] finalSpecifiedVariableNames = specifiedVariableNames;
            final String[] variableNames =
                inputDataset.getVariables()
                    .stream()
                    .filter(variable -> !(variable instanceof CoordinateAxis))
                    .filter(variable -> variable.findDimensionIndex("time") != -1)
                    .map(variable -> variable.getFullName())
                    .filter(variableName -> Arrays.stream(finalSpecifiedVariableNames)
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
                file.getAbsolutePath(),
                outputFilename,
                timeInstants);

            // Instantiate the ApplicationContext.
            final ApplicationContext applicationContext = new ApplicationContext("regrid");
            applicationContext.setTask(task);
            applicationContext.setProductDefinition(productDefinition);
            applicationContext.setMetadataDao(metadataDao);
            applicationContext.setTempPathname(tempDir);
            ApplicationContextBuilder.populateApplicationContext(
                applicationContext,
                task,
                productDefinition
            );

            // Process the files.
            AggregationTaskExecutor aggregationTaskExecutor = new AggregationTaskExecutor();
            aggregationTaskExecutor.execute(task, applicationContext);
        }
    }

}
