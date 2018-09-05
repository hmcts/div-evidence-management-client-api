package uk.gov.hmcts.reform.emclient.service;

import org.springframework.http.ResponseEntity;

public interface EvidenceManagementDeleteService {


    /**
     * This method attempts to delete the document stored in the Evidence Management document store identified by the
     * given file url.
     * <p/>
     *
     * @param fileUrl            a String containing the access details of the file to be deleted
     * @param authorizationToken a String holding the authorisation token of the current user
     * @param requestId          a String used to identify the current operation
     * @return a ResponseEntity instance containing the response received from the Evidence Management service
     */

    ResponseEntity<?> deleteFile(String fileUrl,
                                 String authorizationToken,
                                 String requestId);
}