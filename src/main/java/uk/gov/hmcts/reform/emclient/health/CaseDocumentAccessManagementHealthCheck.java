package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

public class CaseDocumentAccessManagementHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public CaseDocumentAccessManagementHealthCheck(HttpEntityFactory httpEntityFactory,
                                          @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                                          @Value("${case_document_am.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
