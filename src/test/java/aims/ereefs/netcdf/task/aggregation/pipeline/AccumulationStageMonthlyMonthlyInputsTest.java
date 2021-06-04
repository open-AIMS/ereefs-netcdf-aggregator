package aims.ereefs.netcdf.task.aggregation.pipeline;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import aims.ereefs.netcdf.util.NcAggregateProductDefinitionGenerator;
import aims.ereefs.netcdf.util.NcAggregateTaskGenerator;
import aims.ereefs.netcdf.util.netcdf.NetcdfFileGenerator;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;
import au.gov.aims.ereefs.pojo.metadata.MetadataDao;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.junit.Test;
import ucar.ma2.Array;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Test the {@link AccumulationStage} class using {@code Monthly} input files with {@code Daily}
 * data.
 *
 * @author Aaron Smith
 */
public class AccumulationStageMonthlyMonthlyInputsTest extends AbstractAccumulationStageTest {

    /**
     * Properties to use for defining the {@code Product}.
     */
    static protected AggregationPeriods AGGREGATION_PERIOD = AggregationPeriods.ANNUAL;

    /**
     * Verify the calculation of the {@code Mean} using the {@code temp_hour} variable.
     */
    @Test
    public void execute_tempSinceEpochAnnualAggregation_valid() {

        final TestExecutor testExecutor = new TestExecutor(AGGREGATION_PERIOD, this.cachePath);
        this.populateTestExecutor(
            this.inputPath,
            this.metadataDao,
            testExecutor
        );
        final List<List<Array>> actualResultsByDepthGroupList =
            testExecutor.execute("temp_since_epoch");

        // Calculate the expected result.
        DateTime epoch = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.UTC);
        DateTime startDate = NetcdfFileGenerator.DEFAULT_START_DATE;
        int monthsSinceEpoch = Months.monthsBetween(epoch, startDate).getMonths();
        double EXPECTED_RESULT =
            (
                (monthsSinceEpoch) * 31             // Jan
                    + (monthsSinceEpoch + 1) * 28   // Feb
                    + (monthsSinceEpoch + 2) * 31   // Mar
                    + (monthsSinceEpoch + 3) * 30   // Apr
                    + (monthsSinceEpoch + 4) * 31   // May
                    + (monthsSinceEpoch + 5) * 30   // June
                    + (monthsSinceEpoch + 6) * 31   // July
                    + (monthsSinceEpoch + 7) * 31   // Aug
                    + (monthsSinceEpoch + 8) * 30   // Sept
                    + (monthsSinceEpoch + 9) * 31   // Oct
                    + (monthsSinceEpoch + 10) * 30  // Nov
                    + (monthsSinceEpoch + 11) * 31  // Dec
            ) / 365.0;

        // Validate the actual results against the expected results.
        TestUtils.validateResults(
            actualResultsByDepthGroupList,
            TestUtils.buildResultSetFromSingleValue(EXPECTED_RESULT)
        );

    }

    /**
     * Verify the order of the depths using the {@code Mean} of the {@code temp_depth} variable.
     */
    @Test
    public void execute_tempDepthAnnualAggregation_valid() {

        final TestExecutor testExecutor = new TestExecutor(AGGREGATION_PERIOD, this.cachePath);
        this.populateTestExecutor(
            this.inputPath,
            this.metadataDao,
            testExecutor
        );
        final List<List<Array>> actualResultsByDepthGroupList = testExecutor.execute("temp_depth");

        // Build the expected results.
        int depthGroup1Size = 4;
        int expectedCellCount = depthGroup1Size * LATS.length * LONS.length;
        Double[] expectedResultsDepthGroup1 = new Double[expectedCellCount];
        for (int index = 0; index < expectedCellCount; index++) {
            expectedResultsDepthGroup1[index] = DEPTHS[(DEPTHS.length - 1) - (index / 4)];
        }
        expectedCellCount = 2 * LATS.length * LONS.length;
        Double[] expectedResultsDepthGroup2 = new Double[expectedCellCount];
        for (int index = 0; index < expectedCellCount; index++) {
            expectedResultsDepthGroup2[index] = DEPTHS[(DEPTHS.length - 1 - depthGroup1Size) - (index / 4)];
        }
        List<Double[]> expectedResultsByDepthGroup = new ArrayList<Double[]>() {{
            add(expectedResultsDepthGroup1);
            add(expectedResultsDepthGroup2);
        }};

        // Validate the actual results against the expected results.
        TestUtils.validateResults(
            actualResultsByDepthGroupList,
            expectedResultsByDepthGroup
        );

    }

    /**
     * Utility method to populate the specified
     * {@link aims.ereefs.netcdf.task.aggregation.pipeline.AbstractAccumulationStageTest.TestExecutor}
     * for executing tests.
     */
    protected void populateTestExecutor(
        File inputPath,
        MetadataDao metadataDao,
        TestExecutor testExecutor
    ) {

        // Generate the input files and their corresponding Metadata, and store the Metadata in
        // the database.
        final int maxMonths = 12;
        final NetCDFMetadataBean[] netCDFMetadataBeans = new NetCDFMetadataBean[12];
        DateTime startDate = NetcdfFileGenerator.DEFAULT_START_DATE;
        final File[] inputDatasetFiles = new File[maxMonths];
        try {
            for (int monthIndex = 0; monthIndex < maxMonths; monthIndex++) {
                File inputDatasetFile = NetcdfFileGenerator.generateMonthlyMonthly(
                    inputPath,
                    false,
                    startDate.plusMonths(monthIndex),
                    LATS,
                    LONS,
                    DEPTHS
                );
                final String tempKey = "0" + monthIndex;
                NetCDFMetadataBean netCDFMetadataBean = NetCDFMetadataBean.create(
                    PRODUCT_ID,
                    "month" + tempKey.substring(tempKey.length() - 2),
                    new URI("file:" + inputDatasetFile.getAbsolutePath()),
                    inputDatasetFile,
                    DateTime.now().getMillis()
                );
                metadataDao.persist(netCDFMetadataBean.toJSON());
                inputDatasetFiles[monthIndex] = inputDatasetFile;
                netCDFMetadataBeans[monthIndex] = netCDFMetadataBean;
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

        NcAggregateProductDefinition productDefinition = NcAggregateProductDefinition.make(
            PRODUCT_ID,
            "Australia/Brisbane",
            new ProductDefinition.Filters(new ProductDefinition.DateRange[0]),
            new NcAggregateProductDefinition.NetCDFInput[]{
                NcAggregateProductDefinitionGenerator.makeMonthlyMonthlyInput(
                    INPUT_ID,
                    NetcdfFileGenerator.VARIABLE_NAMES
                )
            },
            new ArrayList<NcAggregateProductDefinition.PreProcessingTaskDefn>(),
            NcAggregateProductDefinitionGenerator.makeAggregationAction(
                AGGREGATION_PERIOD,
                NetcdfFileGenerator.VARIABLE_NAMES,
                DEPTHS
            ),
            NcAggregateProductDefinitionGenerator.makeAnnualOutputs()
        );

        // Define a Task to perform the generation of a single output file.
        final NcAggregateTask task = NcAggregateTaskGenerator.generate(
            PRODUCT_ID,
            NcAggregateTaskGenerator.makeTimeInstants(
                INPUT_ID,
                new HashMap<Double, Map<String, Integer>>() {{
                    put(
                        1.0,
                        new TreeMap<String, Integer>() {{
                            for (int index = 0; index < netCDFMetadataBeans.length; index++) {
                                NetCDFMetadataBean netCDFMetadataBean = netCDFMetadataBeans[index];
                                put(
                                    netCDFMetadataBean.getId(),
                                    netCDFMetadataBean
                                        .getVariableMetadataBeanMap()
                                        .get("temp_depth") // Choose a variable that has temporal data.
                                        .getTemporalDomainBean()
                                        .getTimeValues()
                                        .size() - 1   // zero-based.
                                );
                            }
                        }}
                    );
                }}
            )
        );

        testExecutor.populate(
            inputDatasetFiles,
            productDefinition,
            task,
            metadataDao
        );

    }

}
