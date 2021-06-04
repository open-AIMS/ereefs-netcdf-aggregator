package aims.ereefs.netcdf.output.netcdf;

import au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition;
import ucar.nc2.Variable;

import java.util.List;

/**
 * Value object that links a
 * {@link au.gov.aims.ereefs.pojo.definition.product.NcAggregateProductDefinition.SummaryOperator} with the
 * corresponding {@link #inputVariables} and {@link #outputVariables}.
 *
 * @author Aaron Smith
 */
public class SummaryOperatorVariables {
    public NcAggregateProductDefinition.SummaryOperator summaryOperatorConfig;
    public List<Variable> inputVariables;
    public List<Variable> outputVariables;
}

