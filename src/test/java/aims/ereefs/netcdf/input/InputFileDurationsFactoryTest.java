package aims.ereefs.netcdf.input;

import aims.ereefs.netcdf.input.netcdf.InputFileDurations;
import aims.ereefs.netcdf.input.netcdf.InputFileDurationsFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link InputFileDurationsFactory} class.
 *
 * @author Aaron Smith
 */
public class InputFileDurationsFactoryTest {

    /**
     * Test successful factory conditions.
     */
    @Test
    public void testSuccess() {

        // Instantiate class for 100% code coverage.
        new InputFileDurationsFactory();

        // Test each factory scenario.
        for (InputFileDurations d : InputFileDurations.values()) {
            Assertions
                .assertThat(InputFileDurationsFactory.make(d.id))
                .isEqualTo(d);
            Assertions
                .assertThat(InputFileDurationsFactory.make(d.id.toUpperCase()))
                .isEqualTo(d);
        }

        // Test pre-known factory scenarios.
        Assertions
            .assertThat(InputFileDurationsFactory.make("daily"))
            .isEqualTo(InputFileDurations.DAILY);
        Assertions
            .assertThat(InputFileDurationsFactory.make("DAILY"))
            .isEqualTo(InputFileDurations.DAILY);
        Assertions
            .assertThat(InputFileDurationsFactory.make("monthly"))
            .isEqualTo(InputFileDurations.MONTHLY);
        Assertions
            .assertThat(InputFileDurationsFactory.make("MONTHLY"))
            .isEqualTo(InputFileDurations.MONTHLY);

    }

    /**
     * Test failure factory conditions.
     */
    @Test
    public void testFail() {
        final String id = "unsupported_file_duration";
        try {
            InputFileDurationsFactory.make(id);
        } catch(Exception e) {
            Assertions
                .assertThat(e)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("InputFileDuration \"" + id + "\" not supported.");
        }
    }

}
