# Source Code Explanations

## Significant classes

- [Main](src/main/java/aims/ereefs/netcdf/Main.java) - the entry point of the application.
  Implementations of the
  [OperationModeExecutor](src/main/java/aims/ereefs/netcdf/OperationModeExecutor.java) determine the 
  action of the application.
- [ApplicationContext](src/main/java/aims/ereefs/netcdf/ApplicationContext.java) - a global and immutable namespace in 
  which application-level references are shared.
- [OperationModeExecutor](src/main/java/aims/ereefs/netcdf/OperationModeExecutor.java) - interface
  for classes that determine the action of the application. ncAggregate currently supports:
  - Aggregation
  - Regridding
  - Metadata Population

### Aggregation
- [AggregationOperationModeExecutor](src/main/java/aims/ereefs/netcdf/task/aggregation/AggregationOperationModeExecutor.java) -
  coordinating class for performing aggregations.
- [AggregationTaskExecutor](src/main/java/aims/ereefs/netcdf/task/aggregation/AggregationTaskExecutor.java) - 
  co-ordinates the processing for **ncAggregate**.
- [PipelineBuilder](src/main/java/aims/ereefs/netcdf/task/aggregation/pipeline/PipelineBuilder.java) - builds the 
  _Processing Pipeline_ that performs the _Task_ in accordance with the _Product Definition_.
- [PipelineContext](src/main/java/aims/ereefs/netcdf/task/aggregation/pipeline/PipelineContext.java) - a local and 
  mutable namespace in which references local to the _Pipeline_ are shared.
- [AccumulationStage](src/main/java/aims/ereefs/netcdf/task/aggregation/pipeline/AccumulationStage.java) - the stage of 
  the _Processing Pipeline_ that coordinates the reading, accumulation, and aggregation/processing of data.
- [Aggregator](src/main/java/aims/ereefs/netcdf/aggregator/Aggregator.java) - interface for classes that manage temporal 
  aggregation functions.
- [PipelineFactory](src/main/java/aims/ereefs/netcdf/aggregator/operators/factory/PipelineFactory.java) - interface for 
  _Factory_ classes that instantiate and initialise a _Calculation Pipeline_ to perform the actual data processing.

An extension to the aggregation functionality is the Data Extraction.

- [ExtractionSitesBuilderTask](src/main/java/aims/ereefs/netcdf/input/extraction/ExtractionSitesBuilderTask.java) and the
[SiteBasedSummaryAccumulatorImpl](src/main/java/aims/ereefs/netcdf/output/summary/SiteBasedSummaryAccumulatorImpl.java)
classes borrow from the Zone-based summary logic (see
[ZoneBasedSummaryAccumulatorImpl](src/main/java/aims/ereefs/netcdf/output/summary/ZoneBasedSummaryAccumulatorImpl.java)).

### Regridding
- [RegridOperationModeExecutor](src/main/java/aims/ereefs/netcdf/regrid/RegridOperationModeExecutor.java) - coordinating class for
  performing regridding of curvilinear data to rectilinear data.

### Metadata Population
This was added as a means to populate the development environment without needing to run the
DownloadManager component of the AIMS eReefs Platform.
