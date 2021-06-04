#!/usr/bin/env bash

#
# Run ncAggregate as a Java application against a MongoDB-based database. This script makes the
# following assumptions:
#
# - ncAggregate has been packaged via the maven-package.sh script.
# - The database is running locally, listening on the standard port, and already populated.
#

# Identify the directory of this script, which is also the root directory of the project.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")
SCRIPT_NAME=`basename "$0"`
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

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

# Execute the application.
java \
    -DEXECUTION_ENVIRONMENT=test \
    -DDB_TYPE=mongodb \
    -DMONGODB_HOST=localhost \
    -DMONGODB_PORT=27017 \
    -DMONGODB_DB=ereefs \
    -DMONGODB_USER_ID=ncaggregate \
    -DMONGODB_PASSWORD=ncaggregate \
    -DTASK_ID=${TASK_ID} \
    -jar ${PROJECT_ROOT}/target/ereefs-ncaggregate-SNAPSHOT-jar-with-dependencies.jar
