#!/usr/bin/env bash

#
# Build the Docker image using the root Dockerfile. This script assumes the user has previously
# executed the 'maven-package.sh' script.
#

# Identify the directory of this script.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")

# Identify the project root directory based on the location of this script.
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

# Build the Docker image.
APP_NAME="ereefs-ncaggregate"
docker build \
    -t ${APP_NAME} \
    -f ${PROJECT_ROOT}/Dockerfile \
    --force-rm \
    ${PROJECT_ROOT}
