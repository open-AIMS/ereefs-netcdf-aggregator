#!/usr/bin/env bash

#
# Use a Docker image to execute JUnit test cases via Maven.
#
# Notes:
#   - A custom Docker image is built by extending a Maven Docker image and installing the necessary
#     NetCDF libraries.
#   - Maven is executed as the current user to ensure the Maven repository (${HOME}/.m2) is not
#     owned by "root".
#   - To execute a specific test, use: ./maven-test.sh -Dtest=<class name>#<method name>
#

# Identify the directory of this script.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")

# Identify the project root directory based on the location of this script.
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

# Ensure the ${HOME}/.m2 directory belongs to the current user.
mkdir -p ${HOME}/.m2

# Build the Docker image.
IMAGE_NAME="maven:ncaggregate"
docker build \
    -t ${IMAGE_NAME} \
    -f ${SCRIPT_PATH}/Dockerfile.test \
    --force-rm \
    ${SCRIPT_PATH}

# Execute the Maven container to run the test(s).
docker run \
    -u $(id -u):$(id -g) \
    --rm \
    --name "maven-ncaggregate-test" \
    -v "${HOME}/.m2:/tmp/maven/.m2" \
    -v ${PROJECT_ROOT}:/workdir \
    -w /workdir \
    ${IMAGE_NAME} \
    mvn -Duser.home="/tmp/maven" clean test $1
