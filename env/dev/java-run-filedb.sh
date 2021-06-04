#!/usr/bin/env bash

#
# Run ncAggregate as a Java application against a file-based database. This script makes the
# following assumptions:
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

# Execute the application.
java \
    -DEXECUTION_ENVIRONMENT=test \
    -DDB_TYPE=file \
    -DDB_PATH=${DB_LOCATION} \
    -DTASK_ID=${TASK_ID} \
    -jar ${PROJECT_ROOT}/target/ereefs-ncaggregate-SNAPSHOT-jar-with-dependencies.jar
