FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

ENV APP div-evidence-management-client-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 47

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:4006/health || exit 1

EXPOSE 4006

