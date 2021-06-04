/**
 * Processing {@code Pipeline} for reading input datasets, performing calculations, and writing to
 * the output dataset.
 * <p>
 * Stage order is:
 * <ul>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.TimeInstantIteratorStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.TimeInstantExecutorStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.OperatorIteratorStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.OperatorExecutorStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.AccumulationStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.RegularGriddingStage}
 *     </li>
 *     <li>
 *         {@link aims.ereefs.netcdf.task.aggregation.pipeline.WriteTimeSliceStage}
 *     </li>
 * </ul>
 */
package aims.ereefs.netcdf.task.aggregation.pipeline;