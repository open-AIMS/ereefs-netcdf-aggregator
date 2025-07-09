#!/usr/bin/env bash

# Run the Docker image against a MongoDB instance listening on port 27017 on localhost.
#
# Note that the MongoDB instance is expected to be already populated with Metadata, Product
# Definitions, and Tasks.

# Capture any command-line arguments.
while getopts t: option
do
case "${option}"
in
t) TASK_ID=${OPTARG};;
esac
done

# Verify mandatory arguments.
if [[ -z $TASK_ID ]]; then
  echo "Error: Task not specified."
  echo "Usage: ${SCRIPT_NAME} -t <task id>"
  exit 1
fi

docker run \
    -u $(id -u):$(id -g) \
    --name "ereefs-ncaggregate" \
    --memory=7.5GB \
    --network ereefs-network \
    -v /data/ereefs/files/:/data/ereefs/files/ \
    -v /tmp/ereefs-netcdf-aggregator/:/tmp/ereefs-netcdf-aggregator/ \
    --env TASK_ID=${TASK_ID} \
    --env EXECUTION_ENVIRONMENT=test \
    --env MONGODB_HOST=mongodb \
    --env MONGODB_PORT=27017 \
    --env MONGODB_DB=ereefs \
    --env MONGODB_USER_ID=ncaggregate \
    --env MONGODB_PASSWORD=ncaggregate \
    --env PROMETHEUS_PUSH_GATEWAY_URL=http://pushgateway:9091 \
    ereefs-ncaggregate
