package uk.gov.hmcts.reform.emclient.service;


import org.springframework.http.ResponseEntity;


public interface EvidenceManagementDeleteService {

    ResponseEntity<String> deleteDocument(String documentId,
                                          String authorizationToken,
                                          String requestId);
}
