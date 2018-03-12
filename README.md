# Divorce Evidence Management Client API

## A Hypermedia Restful API/HATEOAS Client for Evidence Management Service


## Introduction
* This API provides below endpoints
  * File Upload(s)
  * File Download
* It uses below technical stack
  *  Java8
  * Spring Boot
  * Junit, Mockito and SpringBootTest and Powermockito
  * Apache Maven
  * Spring Hateos
  * Traverson
* Plugins used by project
  * Jacoco
  * OWASP dependency check
  * Sonar
  * Xlint
  * Checkstyle 
  #### :bulb: Checkstyle is currently configured to not fail on violations as the configuration is not yet finalised.

## Project setup
> * git clone [https://github.com/hmcts/div-evidence-management-client-api.git](https://github.com/hmcts/div-evidence-management-client-api.git)
> * cd div-evidence-management-client-api
> * Run `./gradlew build` This command will start the spring boot application in an embedded tomcat on port 4006.To change the port change the configuration in `application.properties`
* Below commands are available
  > `docker pull sonarqube:latest && docker run -d --restart=always -p9000:9000 sonarqube:latest`
  > #### This command will create a local Sonar Qube docker instance on port 9000
  
  > `./gradlew dependencyCheckAnalyze`
  > #### This command will create a dependency check report to identify the use of known vulnerable components.
  
  > `./gradlew sonarqube -Dsonar.host.url=http://localhost:9000`
  >  #### This command will generate sonar reports and update it into local sonarqube instance.
 
  > `./gradlew check`
  >  #### This command will runs all verification tasks in the project, including test.
 
 
## API Consumption

| File Upload Endpoint | HTTP Protocol | Header Attribute  Condition | Headers | Body |
|:----------------------------------:|---------------|:---------------------------:|:------------------------------------:|:----------------------------------------------------------------:|
| /emclientapi/version/1/uploadFiles | POST | Required | AuthorizationToken : { User Token }  | [key=file,value=MultipartFile1,key=file,value=MultipartFile2,....] |
|  |  | Required | Content-Type :multipart/form-data  |  |
|  |  | Optional | RequestId :{RequestId} |  |

###### File Upload Response:

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

###### File Download Response:

``` Actual file for the given URL. ```


##  License
```The MIT License (MIT)

Copyright (c) 2017 HMCTS (HM Courts & Tribunals Service)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
