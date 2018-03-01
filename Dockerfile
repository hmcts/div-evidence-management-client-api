FROM openjdk:8-jdk-alpine

COPY target/evidence-management-client-api-*.jar /app.jar

EXPOSE 4006

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
