package aims.ereefs.netcdf.util;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import au.gov.aims.ereefs.pojo.definition.product.ProductDefinition;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility class for generating a {@code ProductDefinition}.
 *
 * @author Aaron Smith
 */
public class NcAggregateProductDefinitionGenerator {

    /**
     * Utility method that instantiates the {@link NcAggregateProductDefinition}.
     */
    static public NcAggregateProductDefinition generate(String productId,
                                                        NcAggregateProductDefinition.NetCDFInput input,
                                                        NcAggregateProductDefinition.Action action,
                                                        NcAggregateProductDefinition.Outputs outputs,
                                                        String[] variables,
                                                        double[] depths) {

        return NcAggregateProductDefinition.make(
            productId,
            "Australia/Brisbane",
            new ProductDefinition.Filters(new ProductDefinition.DateRange[0]),
            new NcAggregateProductDefinition.NetCDFInput[]{input},
            new ArrayList<NcAggregateProductDefinition.PreProcessingTaskDefn>(),
            action,
            outputs
        );

    }

    static public NcAggregateProductDefinition.NetCDFInput makeHourlyDailyInput(String inputId,
                                                                                String[] variables) {
        return NcAggregateProductDefinition.NetCDFInput.make(
            inputId,
            "netcdf",
            "hourly",
            "daily",
            true,
            variables
        );
    }

    static public NcAggregateProductDefinition.NetCDFInput makeDailyMonthlyInput(String inputId,
                                                                                 String[] variables) {
        return NcAggregateProductDefinition.NetCDFInput.make(
            inputId,
            "netcdf",
            "daily",
            "monthly",
            true,
            variables
        );
    }

    static public NcAggregateProductDefinition.NetCDFInput makeMonthlyMonthlyInput(String inputId,
                                                                                   String[] variables) {
        return NcAggregateProductDefinition.NetCDFInput.make(
            inputId,
            "netcdf",
            "monthly",
            "monthly",
            true,
            variables
        );
    }

    static public NcAggregateProductDefinition.Action makeAggregationAction(AggregationPeriods aggregationPeriod,
                                                                            String[] variables,
                                                                            double[] depths) {
        return new NcAggregateProductDefinition.Action(
            aggregationPeriod.name(),
            depths,
            variables,
            new NcAggregateProductDefinition.SummaryOperator[0]
        );
    }

    static public NcAggregateProductDefinition.Outputs makeDailyOutputs() {
        return new NcAggregateProductDefinition.Outputs(
            NcAggregateProductDefinition.OutputsStrategy.DAILY,
            true,
            "daily-<year>-<month>-<day>",
            new ArrayList<NcAggregateProductDefinition.OutputFile>() {{
                add(new NcAggregateProductDefinition.NetcdfOutputFile());
            }}
        );
    }

    static public NcAggregateProductDefinition.Outputs makeMonthlyOutputs() {
        return new NcAggregateProductDefinition.Outputs(
            NcAggregateProductDefinition.OutputsStrategy.MONTHLY,
            true,
            "daily-<year>-<month>",
            new ArrayList<NcAggregateProductDefinition.OutputFile>() {{
                add(new NcAggregateProductDefinition.NetcdfOutputFile());
            }}
        );
    }

    static public NcAggregateProductDefinition.Outputs makeAnnualOutputs() {
        return new NcAggregateProductDefinition.Outputs(
            NcAggregateProductDefinition.OutputsStrategy.ANNUAL,
            true,
            "daily-<year>-<month>",
            new ArrayList<NcAggregateProductDefinition.OutputFile>() {{
                add(new NcAggregateProductDefinition.NetcdfOutputFile());
            }}
        );
    }

}
