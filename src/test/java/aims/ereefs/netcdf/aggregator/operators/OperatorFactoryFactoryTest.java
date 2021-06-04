package aims.ereefs.netcdf.aggregator.operators;

import aims.ereefs.netcdf.ApplicationContext;
import aims.ereefs.netcdf.aggregator.operators.factory.*;
import aims.ereefs.netcdf.aggregator.operators.factory.threshold.*;
import aims.ereefs.netcdf.aggregator.operators.pipeline.Pipeline;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Tests for the {@link PipelineFactoryFactory} class.
 *
 * @author Aaron Smith
 */
public class OperatorFactoryFactoryTest {

    /**
     * Helper method that invokes
     * {@link PipelineFactoryFactory#make(NcAggregateProductDefinition.SummaryOperator, ApplicationContext)}
     * with a valid configuration, verifying that the expected {@link PipelineFactory}
     * implementation is returned, and that the returned {@link PipelineFactory} can build a
     * {@link Pipeline} without throwing an error.
     */
    private void do_Make_ValidConfig_BuildPipeline(NcAggregateProductDefinition.SummaryOperator config,
                                                   Class expectedFactoryClass) {

        try {
            ApplicationContext dummyApplicationContext = new ApplicationContext("test");
            PipelineFactory factory = PipelineFactoryFactory.make(config, dummyApplicationContext);
            Assertions
                .assertThat(factory)
                .isInstanceOf(expectedFactoryClass);
            Pipeline pipeline = factory.make();
            Assertions
                .assertThat(pipeline)
                .isNotNull();
        } catch (Throwable e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Helper method that invokes
     * {@link PipelineFactoryFactory#make(NcAggregateProductDefinition.SummaryOperator, ApplicationContext)}
     * with an invalid configuration, verifying that the message of the thrown exception
     * {@code startsWith} and {@code endsWith} the expected test.
     */
    private void do_Make_InvalidConfig_RuntimeExceptionThrown(NcAggregateProductDefinition.SummaryOperator config,
                                                              String startsWith,
                                                              String endsWith) {

        Assertions
            .assertThatThrownBy(() -> {
                PipelineFactory factory = PipelineFactoryFactory.make(config, null);
                factory.make();
            })
            .isInstanceOf(RuntimeException.class)
            .hasMessageStartingWith(startsWith)
            .hasMessageEndingWith(endsWith)
            .hasNoCause();
    }

    /**
     * Instantiate the {@link PipelineFactoryFactory} for 100% code coverage.
     */
    @Test
    public void Make_Valid_PipelineFactoryFactory() {
        Assertions
            .assertThat(new PipelineFactoryFactory())
            .isInstanceOf(PipelineFactoryFactory.class);
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a {@link MeanOperatorFactory}.
     */
    @Test
    public void Make_MeanOperator_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            NcAggregateProductDefinition.SummaryOperator.makeMean("temp"),
            MeanOperatorFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a {@link RangeOperatorFactory}.
     */
    @Test
    public void Make_RangeOperator_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            NcAggregateProductDefinition.SummaryOperator.make(
                RangeOperatorFactory.OPERATOR_TYPE,
                new ArrayList<String>() {
                    {
                        add("temp");
                    }
                },
                new ArrayList<>()
            ),
            RangeOperatorFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a {@link SpeedOperatorFactory}.
     */
    @Test
    public void Make_SpeedOperator_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            NcAggregateProductDefinition.SummaryOperator.make(
                SpeedOperatorFactory.OPERATOR_TYPE,
                new ArrayList<String>() {
                    {
                        add("u");
                        add("v");
                    }
                },
                new ArrayList<>()
            ),
            SpeedOperatorFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code SingleZone Threshold Exceedance Count}.
     */
    @Test
    public void Make_SingleZoneThresholdExceedanceCountConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdValueSummaryOperator(
                "SingleZone",
                ThresholdExceedanceCountFactoryAdaptor.VALUE_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                25.0
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code SingleZone Threshold Exceedance Frequency}.
     */
    @Test
    public void Make_SingleZoneThresholdExceedanceFrequencyConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdValueSummaryOperator(
                "SingleZone",
                ThresholdExceedanceFrequencyFactoryAdaptor.VALUE_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                25.0
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code SingleZone Threshold Exceedance Value Accumulation}.
     */
    @Test
    public void Make_SingleZoneThresholdExceedanceValueAccumulationConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdValueSummaryOperator(
                "SingleZone",
                ThresholdExceedanceValueAccumulationFactoryAdaptor.VALUE_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                25.0
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code SingleZone Threshold Exceedance Value Squared Accumulation}.
     */
    @Test
    public void Make_SingleZoneThresholdExceedanceValueSquaredAccumulationConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdValueSummaryOperator(
                "SingleZone",
                ThresholdExceedanceValueSquaredAccumulationFactoryAdaptor.VALUE_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                25.0
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code MultiZone Threshold Exceedance Count}.
     */
//    @Test
    public void Make_MultiZoneThresholdExceedanceCountConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdZonalSummaryOperator(
                "MultiZone",
                ThresholdExceedanceCountFactoryAdaptor.ZONAL_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                "indexToZoneIdMap",
                "zoneIdToThresholdBindKey",
                false
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code MultiZone Threshold Exceedance Frequency}.
     */
//    @Test
    public void Make_MultiZoneThresholdExceedanceFrequencyConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdZonalSummaryOperator(
                "MultiZone",
                ThresholdExceedanceFrequencyFactoryAdaptor.ZONAL_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                "indexToZoneIdMap",
                "zoneIdToThresholdBindKey",
                false
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code MultiZone Threshold Exceedance Value Accumulation}.
     */
//    @Test
    public void Make_MultiZoneThresholdExceedanceValueAccumulationConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdZonalSummaryOperator(
                "MultiZone",
                ThresholdExceedanceValueAccumulationFactoryAdaptor.ZONAL_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                "indexToZoneIdMap",
                "zoneIdToThresholdBindKey",
                false
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that a valid {@link Pipeline} is instantiated via a
     * {@link ThresholdExceedancePipelineFactory} when invoked with configuration for a
     * {@code MultiZone Threshold Exceedance Value Squared Accumulation}.
     */
//    @Test
    public void Make_MultiZoneThresholdExceedanceValueSquaredAccumulationConfig_BuildPipeline() {
        this.do_Make_ValidConfig_BuildPipeline(
            new NcAggregateProductDefinition.ThresholdZonalSummaryOperator(
                "MultiZone",
                ThresholdExceedanceValueSquaredAccumulationFactoryAdaptor.ZONAL_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "greater",
                24,
                "indexToZoneIdMap",
                "zoneIdToThresholdBindKey",
                false
            ),
            ThresholdExceedancePipelineFactory.class
        );
    }

    /**
     * Verify that the process of instantiating a {@link Pipeline} for an {@code unknown} operation
     * results in an expected exception.
     */
    @Test
    public void Make_UnknownConfig_RuntimeExceptionThrown() {
        this.do_Make_InvalidConfig_RuntimeExceptionThrown(
            NcAggregateProductDefinition.SummaryOperator.make(
                "unknown",
                new ArrayList<>(),
                new ArrayList<>()
            ),
            "Operator \"unknown (unknown)\" not supported.",
            "Operator \"unknown (unknown)\" not supported.");
    }

    /**
     * Verify that the process of instantiating a {@link Pipeline} for an {@code unsupported}
     * operation results in an expected exception.
     */
    @Test
    public void Make_UnknownUnsupportedConfig_RuntimeExceptionThrown() {
        this.do_Make_InvalidConfig_RuntimeExceptionThrown(
            NcAggregateProductDefinition.SummaryOperator.make(
                ThresholdExceedanceCountFactoryAdaptor.SUPPORTED_OPERATOR_TYPES[0],
                new ArrayList<>(),
                new ArrayList<>()
            ),
            "Configuration object not supported by this factory.",
            "\"" + NcAggregateProductDefinition.SummaryOperator.class.getName() + "\"."
        );
    }

    /**
     * Verify that the process of instantiating a {@link Pipeline} for an {@code invalid}
     * operation results in an expected exception.
     */
    @Test
    public void Make_InvalidConfig_RuntimeExceptionThrown() {
        this.do_Make_InvalidConfig_RuntimeExceptionThrown(
            new NcAggregateProductDefinition.ThresholdValueSummaryOperator(
                "Invalid",
                ThresholdExceedanceCountFactoryAdaptor.VALUE_OPERATOR_TYPE,
                new ArrayList<>(),
                new ArrayList<>(),
                "less",
                1,
                null
            ),
            "Invalid configuration.",
            "Invalid configuration."
        );
    }

}
