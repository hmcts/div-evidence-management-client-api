package uk.gov.hmcts.reform.emclient.service;


import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;


@Service
@Slf4j
public class EvidenceManagementDeleteServiceImpl implements EvidenceManagementDeleteService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_HEADER = "user-id";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    UserService userService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;


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

        log.info("Deleting evidence management document: fileUrl='{}', requestId='{}'", fileUrl, requestId);

        UserDetails userDetails = null;
        try{
            userDetails = userService.getUserDetails(authorizationToken);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.valueOf(((FeignException)e).status()));
        }
        HttpEntity<Object> httpEntity = deleteServiceCallHeaders(userDetails.getId());
        ResponseEntity<String> response = restTemplate.exchange(fileUrl,
                HttpMethod.DELETE,
                httpEntity,
                String.class);
        log.debug("Document deletion response for userId {}: {}", userDetails.getId(), response);

        return response;
    }


    /**
     * This method generates the http headers required to be provided as part of the delete document request.
     * <p/>
     *
     * @param userId a String holding the userId of the current user
     * @return an HttpEntity instance holding the formatted headers
     */

    private HttpEntity<Object> deleteServiceCallHeaders(String userId) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        httpHeaders.add(USER_ID_HEADER, userId);

        return new HttpEntity<>(httpHeaders);
    }
}