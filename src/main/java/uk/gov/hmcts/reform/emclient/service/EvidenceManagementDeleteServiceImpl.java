package uk.gov.hmcts.reform.emclient.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class EvidenceManagementDeleteServiceImpl implements EvidenceManagementDeleteService {

    private static final Logger log                  = LoggerFactory.getLogger(EvidenceManagementUploadServiceImpl.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${evidence.management.upload.file.url}")
    private String evidenceManagementServiceURL;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public ResponseEntity<String> deleteDocument(String documentId,
                                                 String authorizationToken,
                                                 String requestId) {

        log.info("deleting evidence management document: id='{}', url='{}'", documentId, evidenceManagementServiceURL);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authorizationToken);

        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(evidenceManagementServiceURL,
                                                                HttpMethod.DELETE,
                                                                httpEntity,
                                                                String.class,
                                                                documentId);
        log.debug("document deletion response: {}", response);

        return response;
    }
}
