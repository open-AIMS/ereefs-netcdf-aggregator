package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.aggregator.Aggregator;
import aims.ereefs.netcdf.aggregator.AggregatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.MeanOperatorFactory;
import aims.ereefs.netcdf.aggregator.operators.factory.PipelineFactoryFactory;
import aims.ereefs.netcdf.input.extraction.ExtractionSite;
import aims.ereefs.netcdf.input.extraction.ExtractionSitesBuilderTask;
import aims.ereefs.netcdf.input.netcdf.InputDataset;
import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import aims.ereefs.netcdf.input.netcdf.TimeIncrementFactory;
import aims.ereefs.netcdf.output.summary.SiteBasedSummaryAccumulatorImpl;
import aims.ereefs.netcdf.output.summary.SummaryAccumulator;
import aims.ereefs.netcdf.output.summary.SummaryStatisticsWriter;
import aims.ereefs.netcdf.output.summary.ZoneBasedSummaryAccumulatorImpl;
import aims.ereefs.netcdf.util.Constants;
import aims.ereefs.netcdf.util.netcdf.NetcdfDateUtils;
import aims.ereefs.netcdf.util.netcdf.ReadUtils;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import au.gov.aims.ereefs.pojo.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;
import ucar.units.UnitException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


/**
 * A {@code Pipeline} {@code Stage} that coordinates the reading, accumulation, and
 * aggregation/processing of data for a single {@link PipelineContext#summaryOperator} for a single
 * {@link PipelineContext#timeInstant}.
 *
 * @author Aaron Smith
 */
public class AccumulationStage {

    /**
     * Class-specific {@code logger}.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Cached reference to the {@code ApplicationContext} for contextual information about the
     * application.
     */
    protected ApplicationContext applicationContext;

    /**
     * Cached reference to the {@code PipelineContext} for access to context in which the
     * {@code Stage} is operating.
     */
    protected PipelineContext pipelineContext;

    /**
     * Cached reference to the {@code Cache} that manages access to {@link InputDataset}s.
     */
    protected InputDatasetCache inputDatasetCache;

    /**
     * Cached reference to the {@code AggregationPeriod} for the processing.
     */
    protected AggregationPeriods aggregationPeriod;

    /**
     * Cached reference to the next {@code Stage} of the {@code Pipeline}.
     */
    protected RegularGriddingStage regularGriddingStage;

    /**
     * Constructor to cache references to static system objects/components.
     */
    public AccumulationStage(ApplicationContext applicationContext,
                             PipelineContext pipelineContext,
                             InputDatasetCache inputDatasetCache,
                             AggregationPeriods aggregationPeriod,
                             RegularGriddingStage regularGriddingStage) {
        this.applicationContext = applicationContext;
        this.pipelineContext = pipelineContext;
        this.inputDatasetCache = inputDatasetCache;
        this.aggregationPeriod = aggregationPeriod;
        this.regularGriddingStage = regularGriddingStage;
    }

    /**
     * Invoked to perform the processing.
     */
    public void execute() {

        // Retrieve context information.
        final NcAggregateProductDefinition.SummaryOperator summaryOperator =
            pipelineContext.getSummaryOperator();
        final NcAggregateTask.TimeInstant timeInstant = pipelineContext.getTimeInstant();
        final NcAggregateTask.Input referenceInput = pipelineContext.getInputs().get(0);
        final String referenceInputId = referenceInput.getInputId();

        ChronoUnit timeIncrements = getTimeIncrements(referenceInputId);

        // Declare objects that are lazily instantiated during processing, so we don't have to
        // specifically retrieve related objects ahead of time.
        DateUnit dateUnit = null;
        Aggregator aggregator = null;
        SummaryAccumulator summaryAccumulator;
        final NcAggregateProductDefinition.ZoneBasedSummaryOutputFile zoneBasedSummaryOutputFile =
            this.pipelineContext.getProductDefinition().getOutputs().getZoneBasedSummaryOutputFile();
        final NcAggregateProductDefinition.SiteBasedSummaryOutputFile siteBasedSummaryOutputFile =
            this.pipelineContext.getProductDefinition().getOutputs().getSiteBasedSummaryOutputFile();
        boolean isAggregatorInitialised = false;

        // To avoid OutOfMemory errors, break depth processing into chunks.
        // ----------------------------------------------------------------

        // Once the data has been aggregated, it is converted back to a NetCDF array for regridding
        // and further processing. To do this, we cache some aspects of the original data.
        int[] outputDataShape;
        int timeDimensionIndex;
        int depthDimensionIndex;
        DataType outputDataType;

        // Determine if the variable(s) to be processed have a depth dimension. Some
        // variables, such as Wind Speed, do not have a depth dimension. To determine this, look
        // at the first variable in the first FileIndexBound for the first Input for the current
        // TimeInstant. This variable will belong to the "ReferenceDataset", which will be used to
        // determine the depths to be processed.
        boolean hasDepthDimension;
        List<Double> referenceSelectedDepths;
        try {

            // Identify the ReferenceDataset. The code purposefully does NOT check for NPE as any
            // missing references SHOULD result in an error as it's either a programming or
            // configuration error and should NOT be recovered from.
            try (InputDataset referenceDataset = this.inputDatasetCache.retrieve(
                referenceInput.getFileIndexBounds().get(0).getMetadataId()
            )) {

                // Determine the variable.
                final String fullVariableName = summaryOperator.getInputVariables().get(0);
                final Variable variable = getVariable(referenceDataset, fullVariableName);

                // Use the original shape later when reshaping the aggregated outputs back to the
                // original shape.
                outputDataShape = variable.getShape();
                outputDataType = variable.getDataType();
                timeDimensionIndex = referenceDataset.findTimeDimensionIndex(variable);

                // The variable has depth data if it has a depth dimension (ie: != -1).
                depthDimensionIndex = referenceDataset.findDepthDimensionIndex(variable);
                hasDepthDimension = (depthDimensionIndex != -1);

                // Extract depths to be processed.
                referenceSelectedDepths = referenceDataset.getSelectedDepths(variable);
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to determine if variables have depth.", e);
        }

        // Declare the variables that will be used to communicate which depths are being processed.
        final List<Double> selectedDepthsToProcess = new ArrayList<>();
        final Map<Double, Integer> selectedDepthToIndexMap = new HashMap<>();
        final int MAX_DEPTHS_TO_PROCESS_AT_ONCE = 4;
        int nextDepthToProcess = 0;
        boolean hasMoreDepths = true;
        while (hasMoreDepths) {

            // If the variable has a depth dimension, populate the current depths to process.
            int initialDepthPos = nextDepthToProcess;
            if (hasDepthDimension) {
                selectedDepthsToProcess.clear();
                for (int index = 0; index < MAX_DEPTHS_TO_PROCESS_AT_ONCE; index++) {
                    if ((nextDepthToProcess + index) < referenceSelectedDepths.size()) {
                        selectedDepthsToProcess.add(referenceSelectedDepths.get(nextDepthToProcess + index));
                    }
                }
                nextDepthToProcess += MAX_DEPTHS_TO_PROCESS_AT_ONCE;

                // Add depths to log.
                if (this.logger.isDebugEnabled()) {
                    logger.debug("depths: " + selectedDepthsToProcess
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                    );
                }
            }
            summaryAccumulator = null;

            // Instantiate and initialise the Aggregator if not already done and a
            // NetCDF file is being generated.
            if (this.pipelineContext.isPopulatingOutputDataset()) {
                aggregator = AggregatorFactory.make(
                        aggregationPeriod,
                        dateUnit
                );
                aggregator.setPipelineFactory(
                        PipelineFactoryFactory.make(
                                summaryOperator,
                                this.applicationContext
                        )
                );
            }

            // Instantiate the SummaryAccumulator.
            int layerSize = outputDataShape[outputDataShape.length - 1] *
                    outputDataShape[outputDataShape.length - 2];
            if (zoneBasedSummaryOutputFile != null) {
                summaryAccumulator = new ZoneBasedSummaryAccumulatorImpl(
                        selectedDepthsToProcess,
                        layerSize,
                        (List<String>) applicationContext.getFromCache(
                                zoneBasedSummaryOutputFile.getIndexToZoneIdMapBindName()
                        )
                );
            }
            if (siteBasedSummaryOutputFile != null) {
                summaryAccumulator = new SiteBasedSummaryAccumulatorImpl(
                        selectedDepthsToProcess,
                        layerSize,
                        (List<ExtractionSite>) applicationContext.getFromCache(
                                ExtractionSitesBuilderTask.EXTRACTION_SITES_BIND_NAME
                        )
                );
            }


            for (NcAggregateTask.Input input : pipelineContext.getInputs()) {
                
                // Loop through each FileIndexBounds which points to specific files that contain
                // data for the Operator, and the start and end indexes within those files that
                // contain the data.
                for (NcAggregateTask.FileIndexBounds fileIndexBounds : input.getFileIndexBounds()) {
    
                    // Obtain a reference to the dataset.
                    try (InputDataset inputDataset = this.inputDatasetCache.retrieve(fileIndexBounds.getMetadataId())) {
    
                        // Capture the TimeVariable for the input dataset.
                        final String timeVariableName = inputDataset.getTimeDimension().getFullName();
                        final Variable timeVariable = inputDataset.findVariable(timeVariableName);
    
                        // Construct a DateUnit from the TimeVariable.
                        final String timeUnitsString = timeVariable.getUnitsString();
                        try {
                            dateUnit = new DateUnit(timeUnitsString);
                        } catch (UnitException e) {
                            throw new RuntimeException(
                                "Unable to construct DateUnit from time variable. (variableName: \"" +
                                    timeVariableName + "\"; unitsString: \"" + timeUnitsString + "\".",
                                e
                            );
                        }

                        // Read a slice for each variable.
                        final List<Double[]> variableDataArrayList = new ArrayList<>();
                        List<String> inputVariables = summaryOperator.getInputVariables().stream()
                                .filter(fullVariableName -> {
                                    final String[] variableNameTokens = fullVariableName.split(
                                            Constants.VARIABLE_NAME_SEPARATOR
                                    );
                                    if (variableNameTokens.length != 2) {
                                        throw new RuntimeException("Variable \"" + fullVariableName +
                                                "\" not fully qualified.");
                                    }
                                    return variableNameTokens[0].equalsIgnoreCase(input.getInputId());
                                })
                                .collect(Collectors.toList());
                        
                        selectedDepthToIndexMap.clear();
                        for (Double depth : selectedDepthsToProcess) {
                            final Variable variable = getVariable(inputDataset, inputVariables.get(0));
                            selectedDepthToIndexMap.put(depth, inputDataset.getSelectedDepthToIndexMap(variable).get(depth));
                        }
    
                        // Loop through each time slice of the input dataset.
                        int startIndex = fileIndexBounds.getStartIndex();
                        int endIndex = fileIndexBounds.getEndIndex();
                        for (int readOffset = 0; readOffset <= (endIndex - startIndex); readOffset++) {
                            for (String fullVariableName : inputVariables) {
                                final Variable variable = getVariable(inputDataset, fullVariableName);
                                if (variable == null) {
                                    throw new RuntimeException("Variable \"" + fullVariableName +
                                        "\" not found in dataset (\"" + fileIndexBounds.getMetadataId() + "\").");
                                }
    
                                // Build a shape object for reading a single time slice, based on the shape of the original
                                // reference variable.
                                final int[] originalShape = variable.getShape();
                                final int[] sliceShape = Arrays.copyOf(originalShape, originalShape.length);
    
                                // Modify shape to read a single time slice. We will use 'offset' to step through the time
                                // slices.
                                sliceShape[timeDimensionIndex] = 1;
    
                                // Modify the shape if there is a depth dimension.
                                if (hasDepthDimension) {
                                    sliceShape[depthDimensionIndex] = selectedDepthsToProcess.size();
                                }
    
                                // Complete initialising the Aggregator if not completed.
                                if (aggregator != null && !isAggregatorInitialised) {
    
                                    aggregator.setShape(sliceShape);
                                    aggregator.setDataType(variable.getDataType());
                                    aggregator.initialise();
    
                                    isAggregatorInitialised = true;
    
                                }
    
                                // Read the data.
                                if (logger.isDebugEnabled()) {
                                    logger.debug(variable.getShortName() + " : " + (readOffset + 1) + " of " +
                                        (endIndex - startIndex + 1));
                                }
    
                                Double[] array;
                                if (hasDepthDimension) {
                                    array = ReadUtils.readSingleTimeSlice(
                                        variable,
                                        timeDimensionIndex,
                                        depthDimensionIndex,
                                        readOffset + startIndex,
                                        selectedDepthsToProcess,
                                        selectedDepthToIndexMap
                                    );
                                } else {
                                    array = ReadUtils.readSingleTimeSlice(
                                        variable,
                                        readOffset + startIndex
                                    );
                                }
                                variableDataArrayList.add(array);
    
                            }
    
                            // Add the data to the aggregators.
                            if (aggregator != null) {
    
                                // Provide a workaround when performing MONTHLY aggregates to calculate
                                // ANNUAL aggregates for MEAN operations.
                                if (timeIncrements.equals(ChronoUnit.MONTHS) &&
                                    (summaryOperator.getOperatorType().equalsIgnoreCase(MeanOperatorFactory.OPERATOR_TYPE)) &&
                                    (aggregationPeriod.equals(AggregationPeriods.ANNUAL))) {
                                    logger.debug("Performing special processing for MONTHLY input data.");
                                    // The input data has been aggregated to MONTH from either HOUR or
                                    // DAY. We can't simply add the values together and divide by 12
                                    // because the calculation is wrong. So we convert back to DAY and
                                    // execute the Aggregator that number of times.
                                    // Determine the time instant of the current input time slice.
                                    try {
                                        LocalDateTime dateTime = dateUnit
                                            .makeDate(
                                                timeVariable.read(
                                                    new int[]{readOffset + startIndex},
                                                    new int[]{1}
                                                )
                                                    .getFloat(0)
                                            )
                                            .toInstant()
                                            .atZone(
                                                ZoneId.of(
                                                    pipelineContext.getProductDefinition().getTargetTimeZone()
                                                )
                                            )
                                            .toLocalDateTime();
                                        final LocalDateTime startOfMonth = dateTime
                                            .with(TemporalAdjusters.firstDayOfMonth())
                                            .truncatedTo(ChronoUnit.DAYS);
                                        final LocalDateTime endOfMonth = dateTime
                                            .with(TemporalAdjusters.lastDayOfMonth())
                                            .plus(1, ChronoUnit.DAYS)
                                            .truncatedTo(ChronoUnit.DAYS);
                                        final int daysInMonth = (int) DateTimeUtils.differenceInDays(startOfMonth, endOfMonth);
                                        for (int dayCount = 0; dayCount < daysInMonth; dayCount++) {
                                            aggregator.add(timeInstant.getValue(), variableDataArrayList);
                                        }
                                    } catch (Exception e) {
                                        throw new RuntimeException(
                                            "Failed to calculate the number of days in the input month.",
                                            e
                                        );
                                    }
    
                                } else {
                                    aggregator.add(timeInstant.getValue(), variableDataArrayList);
                                }
                            }
                            if (summaryAccumulator != null) {
                                summaryAccumulator.add(variableDataArrayList);
                            }
    
                            variableDataArrayList.clear();
    
                        }
    
                    }
                }
            }

            // Data for the entire TimeInstant has been read by now, so retrieve the results from the
            // Aggregator and write to the output dataset.
            if (summaryAccumulator != null) {
                try {
                    SummaryStatisticsWriter.write(
                        NetcdfDateUtils.toLocalDateTime(
                            dateUnit,
                            timeInstant.getValue()
                        ),
                        summaryOperator,
                        summaryAccumulator,
                        pipelineContext.getSummaryOutputWriter()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                summaryAccumulator.reset();
            }

            // Has an Aggregator been instantiated? If so, then a NetCDF output file is being
            // generated.
            if (aggregator != null) {

                // Retrieve the results from the Aggregator and convert to NetCDF arrays for the
                // next stage. Force the Aggregator to release its resources immediately to limit
                // the chance of memory issues.
                List<Double[]> aggregatedDataList = aggregator.getAggregatedData();
                aggregator.unInitialise();
                aggregator = null;
                isAggregatorInitialised = false;
                outputDataShape[timeDimensionIndex] = 1;
                if (hasDepthDimension) {
                    outputDataShape[depthDimensionIndex] = selectedDepthsToProcess.size();
                }
                List<Array> arrays = new ArrayList<>(aggregatedDataList.size());
                for (Double[] aggregatedData : aggregatedDataList) {
                    Array array = Array.factory(outputDataType, outputDataShape);
                    arrays.add(array);
                    for (int index = 0; index < aggregatedData.length; index++) {
                        array.setDouble(index, aggregatedData[index]);
                    }
                }

                // Invoke the next stage.
                this.regularGriddingStage.execute(arrays, initialDepthPos);
            }

            // Determine if there are more depths to process.
            hasMoreDepths = hasDepthDimension && nextDepthToProcess < referenceSelectedDepths.size();

        }

    }

    private static Variable getVariable(InputDataset inputDataset, String fullVariableName) {
        final String variableName = getVariableName(fullVariableName);
        return inputDataset.findVariable(variableName);
    }

    private static String getVariableName(String fullVariableName) {
        final String[] variableNameTokens = fullVariableName.split(
            Constants.VARIABLE_NAME_SEPARATOR
        );
        if (variableNameTokens.length != 2) {
            throw new RuntimeException("Variable \"" + fullVariableName +
                "\" not fully qualified.");
        }
        return variableNameTokens[1];
    }


    private ChronoUnit getTimeIncrements(String referenceInputId) {
        NcAggregateProductDefinition.NetCDFInput netcdfInput = null;
        for (NcAggregateProductDefinition.NetCDFInput tmpInput : this.pipelineContext.getProductDefinition().getInputs()) {
            if (tmpInput.getId().equals(referenceInputId)) {
                netcdfInput = tmpInput;
            }
        }
        if (netcdfInput == null) {
            throw new RuntimeException("Input \"" + referenceInputId + "\" not found in product.");
        }

        // Determine the time increments for the input data.
        return TimeIncrementFactory.make(netcdfInput.getTimeIncrement());
    }


}
