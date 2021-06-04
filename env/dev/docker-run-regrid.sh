#!/usr/bin/env bash

#
# Run the Docker image using the --regrid command line argument. Run this script from the root
# directory of the input files.
#

# Identify the directory of this script, which is also the root directory of the project.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")
SCRIPT_NAME=`basename "$0"`
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

docker container prune --force

docker run \
    -u $(id -u):$(id -g) \
    --name "ereefs-ncaggregate" \
    --memory=7.5GB \
    -v `pwd`:/data/ \
    --env EXECUTION_ENVIRONMENT=regrid \
    ereefs-ncaggregate --regrid --input=/data/orig --output=/data/out --cache=/data/regrid-mapper.dat
