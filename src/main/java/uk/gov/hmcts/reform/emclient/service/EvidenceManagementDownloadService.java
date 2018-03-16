package uk.gov.hmcts.reform.emclient.service;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

public interface EvidenceManagementDownloadService {
    ResponseEntity<InputStreamResource> downloadFile(String url, String authorizationToken, String requestId)
            throws IOException;
}
