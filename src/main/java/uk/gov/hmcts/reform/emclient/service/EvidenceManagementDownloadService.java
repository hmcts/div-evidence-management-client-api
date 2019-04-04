package uk.gov.hmcts.reform.emclient.service;

import org.springframework.http.ResponseEntity;

public interface EvidenceManagementDownloadService {

    ResponseEntity<byte[]> downloadFile(String fileId, String authorizationToken);
}
