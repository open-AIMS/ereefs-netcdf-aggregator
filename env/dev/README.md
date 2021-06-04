Development/testing related scripts and configuration files, constructed on the assumption they are used within a Linux 
environment. Developers using Windows should consider using the Vagrant-based VM defined in the 
[ereefs-vm<sup>:lock:</sup>](http://github.com/aims-ks/ereefs-vm) project

## Source code related
- `maven-clean.sh` - executes `maven clean` using a Maven Docker image, deleting the `target` 
directory.
- `maven-javadoc.sh` - executes `maven javadoc:javadoc` using a Maven Docker image, generating the
Javadoc for the source code.
- `maven-package.sh` - executes `maven clean package` using a Maven Docker image, compiling the 
Java source code and building an executable JAR. This script is a pre-requisite for 
`docker-build-image.sh` and the `java-run-*.sh` scripts.
- `maven-test.sh` - executes `maven clean test` using a Maven Docker image to run the test cases
for the source code.

## Docker related
- `docker-build-image.sh` - uses [Dockerfile](../../Dockerfile) to package the JAR built by 
`maven-package.sh` as a Docker image.
 
 ## Execution related
- `docker-run-filedb.sh` - runs the Docker image built by `docker-build-image.sh` against a 
local, file-based database, where the file-based database is available at `/data/ereefs/filedb`.
- `docker-run-mongodb.sh` - runs the Docker image built by `docker-build-image.sh` against a 
local, MongoDB-based database.
- `java-run-filedb.sh` - runs the JAR packaged by `maven-package.sh` against a local, file-based
database, where the file-based database is available at `/data/ereefs/filedb`.
- `java-run-mongodb.sh` - runs the JAR packaged by `maven-package.sh` against a local, 
MongoDB-based database.
