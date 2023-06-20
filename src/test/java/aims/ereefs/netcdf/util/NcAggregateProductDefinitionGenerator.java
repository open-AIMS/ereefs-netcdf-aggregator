package aims.ereefs.netcdf.util;

import aims.ereefs.netcdf.aggregator.AggregationPeriods;
import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;

import java.util.*;

/**
 * Utility class for generating a {@code ProductDefinition}.
 *
 * @author Aaron Smith
 */
public class NcAggregateProductDefinitionGenerator {

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

    static public NcAggregateProductDefinition.Action makeSpecificAggregationAction(AggregationPeriods aggregationPeriod,
                                                                                    String[] variables,
                                                                                    double[] depths,
                                                                                    String operatorName,
                                                                                    String[] outputVariableNames) {
        
        NcAggregateProductDefinition.SummaryOperator[] summaryOperators = new NcAggregateProductDefinition.SummaryOperator[1];

        ArrayList<NcAggregateProductDefinition.OutputVariable> outputVariables = new ArrayList<>();
        for (String outputVariableName : outputVariableNames) {
            NcAggregateProductDefinition.OutputVariable outputVariable = new NcAggregateProductDefinition.OutputVariable();
            outputVariable.setAttributes(new TreeMap<String, String>() {{
                put("short_name", outputVariableName);
            }});
            outputVariables.add(outputVariable);
        }
        
        NcAggregateProductDefinition.SummaryOperator summaryOperator = new NcAggregateProductDefinition.SummaryOperator(
                operatorName,
                operatorName,
                Arrays.asList(variables),
                outputVariables
        );
        summaryOperators[0] = summaryOperator;

        return new NcAggregateProductDefinition.Action(
                aggregationPeriod.name(),
                depths,
                variables,
                summaryOperators
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
