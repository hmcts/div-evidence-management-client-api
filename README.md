# Divorce Evidence Management Client API [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## The Evidence Management Client API is responsible for providing an interface between the Divorce services and the HMCTS Document Management Service

## Introduction
* This API provides below endpoints
  * File Upload(s)
  * File Download
* It uses the following tech stack tech stack
  * Java11
  * Spring Boot
  * Junit, Mockito, SpringBootTest and Powermockito
  * Gradle
  * Spring Hateos
  * Traverson
* Plugins used by project
  * Jacoco
  * OWASP dependency check
  * Sonar

## Setup

1. Clone the repo 
```bash
git clone [https://github.com/hmcts/div-evidence-management-client-api.git](https://github.com/hmcts/div-evidence-management-client-api.git)
```
2. `cd` into the directory
```bash
cd div-evidence-management-client-api
```

3. Run the following command
```bash
./gradlew bootRun

```
This command will start the spring boot application in an embedded tomcat on port 4006.
To change the port change the configuration in `application.properties`. 

This will output: 
```bash
<==========---> 80% EXECUTING [43s]
> :bootRun
```
this is an expected behaviour of Gradle and means the project is running.

**Additional Commands that are available:**
* This command will create a local SonarQube docker instance on port 9000:
```bash
docker pull sonarqube:latest && docker run -d --restart=always -p9000:9000 sonarqube:latest
```

* This command will generate sonar reports and update it into local SonarQube instance.
```bash
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000
```

* This command will runs all verification tasks in the project, including test.
```bash
./gradlew check
```
 
## Development 
**API Consumption**

| File Upload Endpoint | HTTP Protocol | Header Attribute  Condition | Headers | Body |
|:----------------------------------:|---------------|:---------------------------:|:------------------------------------:|:----------------------------------------------------------------:|
| /emclientapi/version/1/uploadFiles | POST | Required | AuthorizationToken : { User Token }  | [key=file,value=MultipartFile1,key=file,value=MultipartFile2,....] |
|  |  | Required | Content-Type :multipart/form-data  |  |
|  |  | Optional | RequestId :{RequestId} |  |

**File Upload Response:**

```JSON
[
    {
        "fileUrl": "http://localhost:8080/documents/214",
        "fileName": "file",
        "mimeType": "application/pdf",
        "createdBy": "13",
        "lastModifiedBy": "13",
        "createdOn": "2017-09-25T22:41:38.569+0000",
        "modifiedOn": "null",
        "status": "OK"
    }
]
```

| File Download Endpoint | HTTP Protocol | Header Attribute  Condition | Headers |
|:-------------------------------------------------------------------------------:|---------------|:---------------------------:|:------------------------------------:|
| /emclientapi/version/1/downloadFile?fileUrl=http://localhost:8080/documents/195 | GET | Required | AuthorizationToken : { User Token }  |
|  |  | Optional | RequestId :{RequestId} |

**File Download Response:**

``` Actual file for the given URL. ```

## Integration test

To run all integration tests locally:

* Make a copy of `src/main/resources/example-application-aat.yml` as `src/main/resources/application-aat.yml`
* Make a copy of `src/integrationTest/resources/example-application-local.properties` as `src/integrationTest/resources/application-local.properties`
* Replace the `replace_me` secrets in both of the _newly created_ files. You can get the values from SCM and Azure secrets key vault (the new files are in .gitignore and should ***not*** be committed to git)
* Start the app with AAT config using `./gradlew clean bootRunAat`
* Start the test with AAT config using `./gradlew clean functional`

## Contract Test (PACT)

To run consumer contract tests locally
* Execute gradle task Contract
```bash
./gradlew contract
```
 
* To publish the contract into local pact broker or Hmcts broker. Execute:
```bash
./gradlew runAndPublishConsumerPactTests
```

### Running additional tests in the Jenkins PR Pipeline

1. Add one or more appropriate labels to your PR in GitHub. Valid labels are:

- ```enable_fortify_scan```
- ```enable_full_functional_tests```

2. Trigger a build of your PR in Jenkins.  Fortify scans will take place asynchronously as part of the Static Checks/Container Build step.
- Check the Blue Ocean view for live monitoring, and review the logs once complete for any issues.
- As Fortify scans execute during the Static Checks/Container Build step, you will need to ensure this is triggered by making a minor change to the PR, such as bumping the chart version.

##  License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
