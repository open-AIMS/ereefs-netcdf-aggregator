pipeline {

    // Default agent is any free agent.
    agent any

    parameters {
        choice(name: 'deployTarget', choices: ['', 'Testing', 'Production'], description: 'Choose the AWS account to deploy to. If no account is selected, it will not be deployed to AWS. IMPORTANT: only the "production" branch can be deployed to the "Production" account.')
        string(name: 'executionEnvironment', defaultValue: 'test', description: 'Enter the environment to create. This is used as a suffix for all components, and should be either "testing", "prod", or a developer name (eg: "asmith").')
    }

    environment {

        // The name of the application, specified here for consistency.
        APP_NAME = 'ereefs-ncaggregate'


        // Maven-related
        // -------------

        // Maven repository.
        MAVEN_REPO = "/workspace/.m2_${params.deployTarget}/repository"

        // BuildId to pass to Maven, generated from date (YYMMDD) and Jenkins BuildNumber.
        // eg: 20190327_33
        JENKINS_BUILD_ID = """${sh(
            returnStdout: true,
            script: 'printf $(date +"%Y%m%d")_${BUILD_NUMBER}'
        )}"""

        // The name Maven uses when packaging the Java app.
        JAR_NAME = "${APP_NAME}-${JENKINS_BUILD_ID}-jar-with-dependencies.jar"

        // AWS-related
        // -----------
        // Credential ID for deploying to AWS.
        AWS_CREDENTIALS_ID_PROD = "jenkins-ereefs-prod-ncaggregate"
        AWS_CREDENTIALS_ID_TEST = "jenkins-ereefs-test-ncaggregate"

        //  AWS CloudFormation Id for project.
        AWS_CLOUD_FORMATION_STACKNAME_PREFIX = "ncaggregate"

        // The deployment target based on the users' selection.
        AWS_DEPLOY_TARGET = "${params.deployTarget == 'Production' ? 'prod' : 'test'}"

        // Docker-related
        // --------------
        // The name of the Docker image that will be built to run the compiled app.
        IMAGE_NAME = "${APP_NAME}-${params.executionEnvironment}"

        // AWS account ID depending on the target environment
        // Parameters for connecting to the AWS ECR (container repository).
        ECR_PROD_URL = "https://${EREEFS_AWS_PROD_ACCOUNT_ID}.dkr.ecr.${EREEFS_AWS_REGION}.amazonaws.com"
        ECR_TEST_URL = "https://${EREEFS_AWS_TEST_ACCOUNT_ID}.dkr.ecr.${EREEFS_AWS_REGION}.amazonaws.com"
        ECR_CREDENTIALS_PROD = "ecr:${EREEFS_AWS_REGION}:${AWS_CREDENTIALS_ID_PROD}"
        ECR_CREDENTIALS_TEST = "ecr:${EREEFS_AWS_REGION}:${AWS_CREDENTIALS_ID_TEST}"

        // Retrieve the credentials for accessing Github Packages from the Jenkins Credentials Manager.
        GITHUB_PACKAGES_CREDENTIALS = credentials('github-packages')
    }

    stages {

        // Use Maven to build and test the app, archiving the results.
        stage('Test') {

            // Maven will be executed within it's Docker container.
            agent {
            // Equivalent to "docker build -f Dockerfile.build --build-arg version=1.0.2 ./build/
                dockerfile {
                    filename 'Dockerfile.test'
                    dir 'env/dev'
                }
            }

            // First install any 3rd party libraries (clean) and then run the tests.
            steps {

                sh '''
                    mvn -DskipTests=true -Dmaven.repo.local=${MAVEN_REPO} clean
                    mvn -B -settings maven-settings.xml -DGITHUB_USERNAME=$GITHUB_PACKAGES_CREDENTIALS_USR -DGITHUB_TOKEN=$GITHUB_PACKAGES_CREDENTIALS_PSW -Dmaven.repo.local=${MAVEN_REPO} clean test
                '''
            }

            // Define the handlers for post-processing.
            post {

                // Always capture the test results.
                always {

                    // Always archive the test results.
                    junit 'target/surefire-reports/*.xml'

                }

            }

        }

        // Use Maven to build and package the app. The resulting JAR file is stashed for use in
        // the Docker build stage.
        stage('Maven package') {

            when {
                anyOf {
                    expression {
                        return params.deployTarget == 'Production' && env.BRANCH_NAME == 'production'
                    }
                    expression {
                        return params.deployTarget == 'Testing'
                    }
                }
            }

            // Maven will be executed within it's Docker container.
            agent {
                docker {
                    image 'maven:3.6-alpine'
                }
            }

            // Compile and package the app.
            steps {
                sh '''
                    mvn -B -settings maven-settings.xml -DGITHUB_USERNAME=$GITHUB_PACKAGES_CREDENTIALS_USR -DGITHUB_TOKEN=$GITHUB_PACKAGES_CREDENTIALS_PSW -Dmaven.repo.local=${MAVEN_REPO} -DbuildId=${JENKINS_BUILD_ID} -DskipTests=true clean package
                '''
            }

            post {

                // If successful, archive the JAR and stash it for later pipeline stages.
                success {
                    archiveArtifacts artifacts: "target/${JAR_NAME}", onlyIfSuccessful: true
                    stash includes: "target/${JAR_NAME}", name: 'artifacts', useDefaultExcludes: false
                }

            }
        }

        // Build the Docker image.
        stage('Docker build') {

            when {
                anyOf {
                    expression {
                        return params.deployTarget == 'Production' && env.BRANCH_NAME == 'production'
                    }
                    expression {
                        return params.deployTarget == 'Testing'
                    }
                }
            }

            steps {

                script {

                    // Retrieve the JAR file from the Stash so it can be included in the Docker
                    // image.
                    unstash 'artifacts'

                    // Build the Docker image.
                    docker.build(IMAGE_NAME, "--force-rm .")

                }

            }
        }

        // Deploy the Docker image and update the CloudFormation Stack.
        stage('Deploy to AWS "TEST" environment') {

            when {
                anyOf {
                    expression {
                        return params.deployTarget == 'Testing'
                    }
                }
            }

            steps {

                script {

                    // Update the CloudFormation Stack.
                    withAWS(region: EREEFS_AWS_REGION, credentials: AWS_CREDENTIALS_ID_TEST) {
                        cfnUpdate(
                            stack: "${AWS_CLOUD_FORMATION_STACKNAME_PREFIX}-${params.executionEnvironment}",
                            params: ["DeployTarget=${AWS_DEPLOY_TARGET}","Environment=${params.executionEnvironment}", "EcrUserId=${AWS_CREDENTIALS_ID_TEST}"],
                            tags: ["deployTarget=${params.deployTarget}","executionEnvironment=${params.executionEnvironment}"],
                            file: 'cloudformation.yaml',
                            timeoutInMinutes: 10,
                            pollInterval: 5000
                        )
                    }

                    // Credentials for connecting to the AWS ECR repository.
                    docker.withRegistry(ECR_TEST_URL, ECR_CREDENTIALS_TEST) {

                        // Deploy the Docker image.
                        docker.image(IMAGE_NAME).push(BUILD_NUMBER)
                        docker.image(IMAGE_NAME).push("latest")
                    }

                }
            }
        }


        // Deploy the Docker image and update the CloudFormation Stack.
        stage('Deploy to AWS "PRODUCTION" environment') {

            when {
                anyOf {
                    expression {
                        return params.deployTarget == 'Production' && env.BRANCH_NAME == 'production'
                    }
                }
            }

            steps {

                script {

                    // Update the CloudFormation Stack.
                    withAWS(region: EREEFS_AWS_REGION, credentials: AWS_CREDENTIALS_ID_PROD) {
                        cfnUpdate(
                             stack: "${AWS_CLOUD_FORMATION_STACKNAME_PREFIX}-${params.executionEnvironment}",
                            params: ["DeployTarget=${AWS_DEPLOY_TARGET}","Environment=${params.executionEnvironment}", "EcrUserId=${AWS_CREDENTIALS_ID_PROD}"],
                             tags: ["deployTarget=${params.deployTarget}","executionEnvironment=${params.executionEnvironment}"],
                             file: 'cloudformation.yaml',
                             timeoutInMinutes: 10,
                             pollInterval: 5000
                        )
                    }

                    // Credentials for connecting to the AWS ECR repository.
                    docker.withRegistry(ECR_PROD_URL, ECR_CREDENTIALS_PROD) {

                        // Deploy the Docker image.
                        docker.image(IMAGE_NAME).push(BUILD_NUMBER)
                        docker.image(IMAGE_NAME).push("latest")
                    }

                }
            }
        }
    }

    // Post-processing.
    post {

        cleanup {

            sh'''

                # Remove any Docker contains that are not in use.
                docker container prune --force

                # Remove any Docker images that are not in use.
                docker image prune --force

                # Remote any Docker networks that are not in use.
                docker network prune --force

            '''
        }

    }

}
