package uk.gov.hmcts.reform.emclient.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * 
 * @author nitinprabhu
 *
 */
public interface EvidenceManagementDownloadService {
    ResponseEntity<InputStreamResource> downloadFile(String url, String authorizationToken, String requestId)
            throws IOException;
}
