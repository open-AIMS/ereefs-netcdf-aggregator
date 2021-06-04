#!/usr/bin/env bash

#
# Invoke 'maven clean' using a Maven Docker image.
#
# Notes:
#   - Maven is executed as the current user to ensure the Maven repository (${HOME}/.m2) is not owned by "root".
#

# The version of the Maven Docker image to use.
MAVEN_DOCKER_TAG="3-jdk-8-slim"

# Identify the directory of this script.
SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "${SCRIPT}")
PROJECT_ROOT=$(readlink --canonicalize "${SCRIPT_PATH}/../..")

# Ensure the ${HOME}/.m2 directory belongs to the current user.
mkdir -p ${HOME}/.m2

# Execute "mvn clean".
docker run \
    -u $(id -u):$(id -g) \
    --rm \
    --name "maven-ncaggregate-clean" \
    -v "${HOME}/.m2:/tmp/maven/.m2" \
    -v ${PROJECT_ROOT}:/workdir \
    -w /workdir \
    maven:${MAVEN_DOCKER_TAG} \
    mvn -Duser.home="/tmp/maven" -DskipTests=true clean
