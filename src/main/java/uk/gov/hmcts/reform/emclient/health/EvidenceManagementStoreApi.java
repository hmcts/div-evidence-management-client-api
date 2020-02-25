package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EvidenceManagementStoreApi extends WebServiceHealthCheck {
    @Autowired
    public EvidenceManagementStoreApi(HttpEntityFactory httpEntityFactory,
                                      @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                                      @Value("${evidence.management.store.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
