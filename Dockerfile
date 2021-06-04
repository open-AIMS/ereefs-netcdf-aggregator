#
# This works, please don't change.

# Dockerfile for the container that runs the packaged eReefs Job Planner.
FROM openjdk:8-slim

# Install required libraries.
RUN apt-get update && apt-get install -y libnetcdf-c++4
#RUN apt-get update && apt-get install -y libnetcdf-dev

# Set the work directory.
WORKDIR /opt/app/bin

# Create a non-root user with no password and no home directory.
RUN \
    groupadd ereefs && \
    useradd --system ereefs -g ereefs && \
    chown ereefs:ereefs /opt/app/bin

# Add the main JAR file.
COPY --chown=ereefs:ereefs target/*-with-dependencies.jar /opt/app/bin/

# Create an 'entrypoint.sh' script that executes the JAR file. Note that "80%" Max RAM was selected by trial and error (specificially, OutOfMemoryError).
RUN echo "#!/bin/bash\njava -XX:MaxRAMPercentage=80.0 -jar /opt/app/bin/`ls -t1 *-with-dependencies.jar | head -n 1` \${*}" > entrypoint.sh
RUN chmod +x /opt/app/bin/entrypoint.sh

# Use the new user when executing.
USER ereefs

# Use the 'entrypoint.sh' script when executing.
ENTRYPOINT ["/opt/app/bin/entrypoint.sh"]
