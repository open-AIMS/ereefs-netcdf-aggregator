package aims.ereefs.netcdf.grid.regular;

import aims.ereefs.netcdf.regrid.ArrayWrapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for the {@link ArrayWrapper} class.
 *
 * @author Aaron Smith
 */
public class ArrayWrapperTest {

    @Test
    public void testMinimumValue() {
        ArrayWrapper longitudeArrayWrapper = new ArrayWrapper(CurvilinearTestData.longitudeArray);
        ArrayWrapper latitudeArrayWrapper = new ArrayWrapper(CurvilinearTestData.latitudeArray);
        Assertions
            .assertThat(longitudeArrayWrapper.getMinimumValue())
            .isEqualTo(CurvilinearTestData.lonValues[0]);
        Assertions
            .assertThat(latitudeArrayWrapper.getMinimumValue())
            .isEqualTo(CurvilinearTestData.latValues[0]);
    }

    @Test
    public void testMaximumValue() {
        ArrayWrapper longitudeArrayWrapper = new ArrayWrapper(CurvilinearTestData.longitudeArray);
        ArrayWrapper latitudeArrayWrapper = new ArrayWrapper(CurvilinearTestData.latitudeArray);
        Assertions
            .assertThat(longitudeArrayWrapper.getMaximumValue())
            .isEqualTo(CurvilinearTestData.lonValues[CurvilinearTestData.lonValues.length - 1]);
        Assertions
            .assertThat(latitudeArrayWrapper.getMaximumValue())
            .isEqualTo(CurvilinearTestData.latValues[CurvilinearTestData.latValues.length - 1]);
    }

}
