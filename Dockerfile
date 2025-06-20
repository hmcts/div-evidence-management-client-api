ARG APP_INSIGHTS_AGENT_VERSION=3.7.0
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

ENV APP div-evidence-management-client-api.jar
COPY lib/applicationinsights.json /opt/app/

COPY build/libs/$APP /opt/app/

EXPOSE 4006

CMD ["div-evidence-management-client-api.jar"]
