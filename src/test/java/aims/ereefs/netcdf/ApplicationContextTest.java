package aims.ereefs.netcdf;

import aims.ereefs.netcdf.input.netcdf.InputDatasetCache;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.task.NcAggregateTask;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases related to the {@link ApplicationContext} class.
 */
public class ApplicationContextTest {

    @Test
    public void testDatasetMetadataIdsByInputIdMap() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        // Should initially be null.
        Assertions.assertThat(applicationContext.getDatasetMetadataIdsByInputIdMap()).isNull();

        // Set values and ensure they can be retrieved.
        final Map<String, List<String>> map = new HashMap<>();
        final List<String> values = new ArrayList<>();
        final String VALUE_1 = "value1";
        final String VALUE_2 = "value2";
        values.add(VALUE_1);
        values.add(VALUE_2);
        final String VALID_KEY = "validKey";
        final String INVALID_KEY = "invalidKey";
        map.put(VALID_KEY, values);
        applicationContext.setDatasetMetadataIdsByInputIdMap(map);
        Assertions
            .assertThat(applicationContext.getDatasetMetadataIdsByInputIdMap().get(VALID_KEY))
            .containsExactlyInAnyOrder(VALUE_1, VALUE_2);
        Assertions
            .assertThat(applicationContext.getDatasetMetadataIdsByInputIdMap().get(INVALID_KEY))
            .isNull();

        // Unable to set a new value.
        try {
            applicationContext.setDatasetMetadataIdsByInputIdMap(new HashMap<>());
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Immutable properties may not be modified.");
        }
    }

    @Test
    public void testInputDatasetCache() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        // Should initially be null.
        Assertions.assertThat(applicationContext.getInputDatasetCache()).isNull();

        // Set value and ensure it can be retrieved.
        applicationContext.setInputDatasetCache(
            new InputDatasetCache(
                null,
                null,
                null,
                null
            )
        );
        Assertions
            .assertThat(applicationContext.getInputDatasetCache())
            .isInstanceOf(InputDatasetCache.class);

        // Unable to set a new value.
        try {
            applicationContext.setInputDatasetCache(
                new InputDatasetCache(
                    null,
                    null,
                    null,
                    null
                )
            );
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Immutable properties may not be modified.");
        }
    }

    @Test
    public void testInputDefinitionByVariableNameMap() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        // Should initially be null.
        Assertions.assertThat(applicationContext.getInputDefinitionByVariableNameMap()).isNull();

        // Set values and ensure they can be retrieved.
        final Map<String, NcAggregateProductDefinition.Input> map = new HashMap<>();
        final String VALID_KEY = "validKey";
        final String INVALID_KEY = "invalidKey";
        final NcAggregateProductDefinition.NetCDFInput inputDefinition =
            NcAggregateProductDefinition.NetCDFInput.make(
                "id",
                "type",
                "hourly",
                "fileDuration",
                true,
                new String[0]
            );
        map.put(VALID_KEY, inputDefinition);
        applicationContext.setInputDefinitionByVariableNameMap(map);
        Assertions
            .assertThat(applicationContext.getInputDefinitionByVariableNameMap().get(VALID_KEY))
            .isInstanceOf(NcAggregateProductDefinition.NetCDFInput.class)
            .isInstanceOf(NcAggregateProductDefinition.Input.class)
            .hasFieldOrPropertyWithValue("id", "id")
            .hasFieldOrPropertyWithValue("type", "type")
            .hasFieldOrPropertyWithValue("timeIncrement", "hourly")
            .hasFieldOrPropertyWithValue("fileDuration", "fileDuration")
            .hasFieldOrPropertyWithValue("completeFilesOnly", true);

        Assertions
            .assertThat(applicationContext.getInputDefinitionByVariableNameMap().get(INVALID_KEY))
            .isNull();

        // Unable to set a new value.
        try {
            applicationContext.setInputDefinitionByVariableNameMap(new HashMap<>());
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Immutable properties may not be modified.");
        }
    }

    @Test
    public void testProductDefinition() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        // Should initially be null.
        Assertions.assertThat(applicationContext.getProductDefinition()).isNull();

        // Set value and ensure it can be retrieved.
        final String ID_1 = "ID_1";
        final String ID_2 = "ID_2";
        applicationContext.setProductDefinition(new NcAggregateProductDefinition(ID_1));
        Assertions
            .assertThat(applicationContext.getProductDefinition().getId())
            .isEqualTo(ID_1);

        // Unable to set a new value.
        try {
            applicationContext.setProductDefinition(new NcAggregateProductDefinition(ID_2));
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Immutable properties may not be modified.");
        }
    }

    @Test
    public void testTask() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        // Should initially be null.
        Assertions.assertThat(applicationContext.getTask()).isNull();

        // Set value and ensure it can be retrieved.
        final String ID_1 = "id1";
        final String ID_2 = "id2";
        applicationContext.setTask(new NcAggregateTask(ID_1));
        Assertions
            .assertThat(applicationContext.getTask().getId())
            .isEqualTo(ID_1);

        // Unable to set a new value.
        try {
            applicationContext.setTask(new NcAggregateTask(ID_2));
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Immutable properties may not be modified.");
        }
    }

    @Test
    public void testCache() {
        final ApplicationContext applicationContext = new ApplicationContext("test");

        final String VALID_KEY = "validKey";
        final String INVALID_KEY = "invalidKey";
        final String VALUE = "value";

        // Should initially be null.
        Assertions
            .assertThat(applicationContext.getFromCache(VALID_KEY))
            .isNull();

        // Set value and ensure it can be retrieved.
        applicationContext.putInCache(VALID_KEY, VALUE);
        Assertions
            .assertThat(applicationContext.getFromCache(VALID_KEY))
            .isEqualTo(VALUE);

        // Invalid key is null.
        Assertions
            .assertThat(applicationContext.getFromCache(INVALID_KEY))
            .isNull();

        // Unable to set a new value.
        try {
            applicationContext.putInCache(VALID_KEY, VALUE);
            Assertions.fail("RuntimeException expected.");
        } catch (RuntimeException expected) {
            Assertions
                .assertThat(expected)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("The key \"")
                .hasMessageEndingWith("\" already exists in cache.");
        }
    }

}
