FROM openjdk:8-jre-alpine

COPY build/install/div-evidence-management-client-api /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:4006/health || exit 1

EXPOSE 4006

ENTRYPOINT ["/opt/app/bin/div-evidence-management-client-api"]