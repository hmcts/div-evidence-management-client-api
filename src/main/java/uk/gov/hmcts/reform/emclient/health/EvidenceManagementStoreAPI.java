package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EvidenceManagementStoreAPI extends WebServiceHealthCheck {
    @Autowired
    public EvidenceManagementStoreAPI(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                      @Value("${evidence.management.store.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
