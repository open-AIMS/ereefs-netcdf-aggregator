#!/usr/bin/env bash

#
# Run the Docker image against a file-based database. This script makes the following assumptions:
#
# - ncAggregate has been packaged via the maven-package.sh script.
# - The root path to the database is /data/ereefs/filedb.
#

# Identify the directory of this script, which is also the root directory of the project.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")
SCRIPT_NAME=`basename "$0"`
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

# Set any default values.
DB_LOCATION=/data/ereefs/filedb
# Capture any command-line arguments.
while getopts d:t: option
do
case "${option}"
in
d) DB_LOCATION=${OPTARG};;
t) TASK_ID=${OPTARG};;
esac
done

# Verify mandatory arguments.
if [[ -z $DB_LOCATION ]]; then
  echo "Error: DB location not specified."
  echo "Usage: ${SCRIPT_NAME} -d <database path>"
  exit 1
fi
if [[ -z $TASK_ID ]]; then
  echo "Error: Task not specified."
  echo "Usage: ${SCRIPT_NAME} -t <task id>"
  exit 1
fi

docker container prune --force

docker run \
    -u $(id -u):$(id -g) \
    --name "ereefs-ncaggregate" \
    --memory=7.5GB \
    -v /data/ereefs/files/:/data/ereefs/files/ \
    -v /tmp/ereefs-netcdf-aggregator/:/tmp/ereefs-netcdf-aggregator/ \
    --env DB_TYPE=file \
    --env DB_PATH=/data \
    --env TASK_ID=${TASK_ID} \
    --env EXECUTION_ENVIRONMENT=test \
    --env PROMETHEUS_PUSH_GATEWAY_URL=http://pushgateway:9091 \
    ereefs-ncaggregate
