package uk.gov.hmcts.reform.emclient.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EvidenceManagementStoreGW extends WebServiceHealthCheck {
    @Autowired
    public EvidenceManagementStoreGW(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                     @Value("${evidence.management.health.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
