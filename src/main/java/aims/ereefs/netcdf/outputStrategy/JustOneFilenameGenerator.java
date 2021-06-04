package aims.ereefs.netcdf.outputStrategy;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import ucar.nc2.units.DateUnit;

/**
 * Created by gcoleman on 7/11/2016.
 */
public class JustOneFilenameGenerator extends AbstractOutputFileNameGenerator {

    public JustOneFilenameGenerator(String pattern, DateUnit dateUnit) {
        super(pattern, dateUnit);
    }

    @Override
    public String generateForTime(double time) {
        return this.getPattern();
    }

    @Override
    public int calculateExpectedTimeIndexes(double time,
                                            AggregationPeriods aggregationPeriods,
                                            int hoursPerTimeIncrement) {

        // Impossible to determine with available information, so return 0 as "DO NOT CHECK".
        return 0;

    }

}
