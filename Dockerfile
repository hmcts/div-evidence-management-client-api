ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
FROM hmctspublic.azurecr.io/base/java:11-distroless

ENV APP div-evidence-management-client-api.jar

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 4006

CMD ["div-evidence-management-client-api.jar"]
