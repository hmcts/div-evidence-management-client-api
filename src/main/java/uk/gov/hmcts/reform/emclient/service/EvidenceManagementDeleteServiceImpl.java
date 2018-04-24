package uk.gov.hmcts.reform.emclient.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class EvidenceManagementDeleteServiceImpl implements EvidenceManagementDeleteService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceManagementUploadServiceImpl.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Autowired
    private RestTemplate restTemplate;


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

    @Override
    public ResponseEntity<?> deleteFile(String fileUrl,
                                        String authorizationToken,
                                        String requestId) {

        log.info("deleting evidence management document: fileUrl='{}', requestId='{}'", fileUrl, requestId);

        HttpEntity<Object> httpEntity = getHeaders(authorizationToken);
        ResponseEntity<String> response = restTemplate.exchange(fileUrl,
                HttpMethod.DELETE,
                httpEntity,
                String.class);
        log.debug("document deletion response: {}", response);

        return response;
    }


    /**
     * This method generates the http headers required to be provided as part of the delete document request.
     * <p/>
     *
     * @param authorizationToken a String holding the authorisation token of the current user
     * @return an HttpEntity instance holding the formatted headers
     */

    private HttpEntity<Object> getHeaders(String authorizationToken) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, authorizationToken);

        return new HttpEntity<>(httpHeaders);
    }
}