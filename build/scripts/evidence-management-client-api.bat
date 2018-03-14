@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  evidence-management-client-api startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and EVIDENCE_MANAGEMENT_CLIENT_API_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\evidence-management-client-api-0.1.4-SNAPSHOT-SNAPSHOT.jar;%APP_HOME%\lib\spring-boot-starter-web-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-actuator-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-boot-actuator-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-cloud-starter-hystrix-1.2.5.RELEASE.jar;%APP_HOME%\lib\spring-cloud-starter-archaius-1.2.5.RELEASE.jar;%APP_HOME%\lib\java-logging-spring-1.2.1.jar;%APP_HOME%\lib\java-logging-httpcomponents-1.2.1.jar;%APP_HOME%\lib\java-logging-1.2.1.jar;%APP_HOME%\lib\logstash-logback-encoder-4.8.jar;%APP_HOME%\lib\hystrix-metrics-event-stream-1.5.6.jar;%APP_HOME%\lib\hystrix-serialization-1.5.6.jar;%APP_HOME%\lib\jackson-module-afterburner-2.8.10.jar;%APP_HOME%\lib\hystrix-javanica-1.5.6.jar;%APP_HOME%\lib\hystrix-core-1.5.6.jar;%APP_HOME%\lib\archaius-core-0.7.4.jar;%APP_HOME%\lib\jackson-databind-2.9.4.jar;%APP_HOME%\lib\jackson-annotations-2.9.4.jar;%APP_HOME%\lib\json-path-assert-2.2.0.jar;%APP_HOME%\lib\commons-io-2.5.jar;%APP_HOME%\lib\hibernate-validator-6.0.5.Final.jar;%APP_HOME%\lib\validation-api-2.0.0.Final.jar;%APP_HOME%\lib\commons-lang3-3.0.jar;%APP_HOME%\lib\spring-boot-devtools-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-aop-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-retry-1.2.1.RELEASE.jar;%APP_HOME%\lib\spring-hateoas-0.23.0.RELEASE.jar;%APP_HOME%\lib\spring-plugin-core-1.2.0.RELEASE.jar;%APP_HOME%\lib\http-proxy-spring-boot-autoconfigure-1.1.0.jar;%APP_HOME%\lib\jackson-dataformat-cbor-2.8.10.jar;%APP_HOME%\lib\jackson-core-2.8.10.jar;%APP_HOME%\lib\json-path-2.2.0.jar;%APP_HOME%\lib\hamcrest-library-1.3.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\spring-cloud-starter-1.1.7.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-boot-starter-logging-1.5.9.RELEASE.jar;%APP_HOME%\lib\logback-classic-1.1.11.jar;%APP_HOME%\lib\jcl-over-slf4j-1.7.25.jar;%APP_HOME%\lib\jul-to-slf4j-1.7.25.jar;%APP_HOME%\lib\log4j-over-slf4j-1.7.25.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-webmvc-4.3.13.RELEASE.jar;%APP_HOME%\lib\spring-web-4.3.13.RELEASE.jar;%APP_HOME%\lib\spring-cloud-netflix-core-1.2.5.RELEASE.jar;%APP_HOME%\lib\spring-boot-autoconfigure-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-boot-1.5.9.RELEASE.jar;%APP_HOME%\lib\spring-context-4.3.13.RELEASE.jar;%APP_HOME%\lib\spring-aop-4.3.13.RELEASE.jar;%APP_HOME%\lib\aspectjweaver-1.8.13.jar;%APP_HOME%\lib\spring-beans-4.3.13.RELEASE.jar;%APP_HOME%\lib\spring-expression-4.3.13.RELEASE.jar;%APP_HOME%\lib\spring-core-4.3.13.RELEASE.jar;%APP_HOME%\lib\httpclient-4.5.3.jar;%APP_HOME%\lib\json-smart-2.2.1.jar;%APP_HOME%\lib\jboss-logging-3.3.1.Final.jar;%APP_HOME%\lib\classmate-1.3.4.jar;%APP_HOME%\lib\snakeyaml-1.17.jar;%APP_HOME%\lib\tomcat-embed-websocket-8.5.23.jar;%APP_HOME%\lib\tomcat-embed-core-8.5.23.jar;%APP_HOME%\lib\tomcat-embed-el-8.5.23.jar;%APP_HOME%\lib\spring-cloud-context-1.1.7.RELEASE.jar;%APP_HOME%\lib\spring-cloud-commons-1.1.7.RELEASE.jar;%APP_HOME%\lib\spring-security-rsa-1.0.3.RELEASE.jar;%APP_HOME%\lib\commons-configuration-1.8.jar;%APP_HOME%\lib\guava-18.0.jar;%APP_HOME%\lib\rxjava-1.2.0.jar;%APP_HOME%\lib\HdrHistogram-2.1.9.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\accessors-smart-1.1.jar;%APP_HOME%\lib\asm-5.0.4.jar;%APP_HOME%\lib\logback-core-1.1.11.jar;%APP_HOME%\lib\httpcore-4.4.8.jar;%APP_HOME%\lib\commons-codec-1.10.jar;%APP_HOME%\lib\tomcat-annotations-api-8.5.23.jar;%APP_HOME%\lib\spring-security-crypto-4.2.3.RELEASE.jar;%APP_HOME%\lib\bcpkix-jdk15on-1.55.jar;%APP_HOME%\lib\commons-lang-2.6.jar;%APP_HOME%\lib\bcprov-jdk15on-1.55.jar;%APP_HOME%\lib\jsr305-3.0.1.jar

@rem Execute evidence-management-client-api
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %EVIDENCE_MANAGEMENT_CLIENT_API_OPTS%  -classpath "%CLASSPATH%" uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable EVIDENCE_MANAGEMENT_CLIENT_API_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%EVIDENCE_MANAGEMENT_CLIENT_API_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
