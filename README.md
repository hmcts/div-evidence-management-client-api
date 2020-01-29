# Divorce Evidence Management Client API [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## The Evidence Management Client API is responsible for providing an interface between the Divorce services and the HMCTS Document Management Service

## Introduction
* This API provides below endpoints
  * File Upload(s)
  * File Download
* It uses the following tech stack tech stack
  * Java8
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
```
git clone [https://github.com/hmcts/div-evidence-management-client-api.git](https://github.com/hmcts/div-evidence-management-client-api.git)
```
2. cd into the directory
```
cd div-evidence-management-client-api
```

3. 
Run the following command
```
./gradlew bootRun

```
This command will start the spring boot application in an embedded tomcat on port 4006.
To change the port change the configuration in `application.properties`. 
This will output 
```
<==========---> 80% EXECUTING [43s]`
> :bootRun
```
 but this is expected behaviour of Gradle and means the project is running.

**Additional Commands that are avaliable:**
* This command will create a local Sonar Qube docker instance on port 9000:
```
docker pull sonarqube:latest && docker run -d --restart=always -p9000:9000 sonarqube:latest
```

* This command will generate sonar reports and update it into local sonarqube instance.
```
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000
```

* This command will runs all verification tasks in the project, including test.
```
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

``` [
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


##  License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.