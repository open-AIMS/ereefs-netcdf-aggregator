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
import org.junit.Test;
import ucar.ma2.Array;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

/**
 * Test the {@link AccumulationStage} class using {@code Monthly} input files with {@code Daily}
 * data.
 *
 * @author Aaron Smith
 */
public class AccumulationStageDailyMonthlyInputsTest extends AbstractAccumulationStageTest {

    /**
     * Properties to use for defining the {@code Product}.
     */
    static protected AggregationPeriods AGGREGATION_PERIOD = AggregationPeriods.MONTHLY;

    /**
     * Verify the calculation of the {@code Mean} using the {@code temp_hour} variable.
     */
    @Test
    public void execute_tempTimeMonthlyAggregation_valid() {

        final TestExecutor testExecutor = new TestExecutor(AGGREGATION_PERIOD, this.cachePath);
        this.populateTestExecutor(
            this.inputPath,
            this.metadataDao,
            testExecutor
        );
        final List<List<Array>> actualResultsByDepthGroupList =
            testExecutor.execute("temp_since_start");

        // Calculate the expected result.
        final double EXPECTED_RESULT =
            DoubleStream.iterate(0, i -> i + 1)
                .limit(31)
                .average()
                .orElse(0.0);

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
    public void execute_tempDepthMonthlyAggregation_valid() {

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

        final String DATASET_ID = "january";

        // Generate the input files and their corresponding Metadata, and store the Metadata in
        // the database.
        final File[] inputDatasetFiles = new File[1];
        try {
            File inputDatasetFile = NetcdfFileGenerator.generateDailyMonthly(
                inputPath,
                false,
                LATS,
                LONS,
                DEPTHS
            );
            NetCDFMetadataBean netCDFMetadataBean = NetCDFMetadataBean.create(
                PRODUCT_ID,
                DATASET_ID,
                new URI("file:" + inputDatasetFile.getAbsolutePath()),
                inputDatasetFile,
                DateTime.now().getMillis()
            );
            metadataDao.persist(netCDFMetadataBean.toJSON());
            inputDatasetFiles[0] = inputDatasetFile;
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

        NcAggregateProductDefinition productDefinition = NcAggregateProductDefinition.make(
            PRODUCT_ID,
            "Australia/Brisbane",
            new ProductDefinition.Filters(new ProductDefinition.DateRange[0]),
            new NcAggregateProductDefinition.NetCDFInput[]{
                NcAggregateProductDefinitionGenerator.makeDailyMonthlyInput(
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
            NcAggregateProductDefinitionGenerator.makeMonthlyOutputs()
        );

        // Define a Task to perform the generation of a single output file.
        final NcAggregateTask task = NcAggregateTaskGenerator.generate(
            PRODUCT_ID,
            NcAggregateTaskGenerator.makeTimeInstants(
                INPUT_ID,
                new HashMap<Double, Map<String, Integer>>() {{
                    put(
                        1.0,
                        new HashMap<String, Integer>() {{
                            put(PRODUCT_ID + "/" + DATASET_ID, 30); // 31 days in January.
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
