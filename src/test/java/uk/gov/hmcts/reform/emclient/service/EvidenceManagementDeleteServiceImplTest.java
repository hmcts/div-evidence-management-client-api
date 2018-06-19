package uk.gov.hmcts.reform.emclient.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDeleteServiceImplTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementDeleteServiceImpl deleteService = new EvidenceManagementDeleteServiceImpl();

    @Before
    public void setUp(){
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id("19").build());
    }


    /**
     * This test issues a document delete request that is expected to succeed. It ensures that the OK response from
     * the EM document store service passes cleanly through the evidence management client api to the caller
     * without any issues or exceptions occurring.
     * <p/>
     */

    @Test
    public void shouldPassThruDocumentDeletedSuccessfullyState() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.OK);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "AAAABBBB", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to do nothing. It ensures that the NO_CONTENT
     * response from the EM document store service passes cleanly through the evidence management client api to
     * the caller without any issues or exceptions occurring.
     * <p/>
     */

    @Test
    public void shouldPassThruNoDocumentIdIsPassedState() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.NO_CONTENT);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "AAAABBBB", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthorised.
     * It ensures that the UNAUTHORIZED response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */

    @Test
    public void shouldPassThruNotAuthorisedAuthTokenState() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.UNAUTHORIZED);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "CCCCDDDD", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthenticated.
     * It ensures that the FORBIDDEN response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */

    @Test
    public void shouldPassThruNotAuthenticatedAuthTokenState() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.FORBIDDEN);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to cause an exception due to the EM document service
     * being unavailable. It ensures that the expected exception passes cleanly through to the caller.
     * <p/>
     */

    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");

        doThrow(ResourceAccessException.class)
                .when(restTemplate)
                .exchange(Mockito.eq(fileUrl),
                        Mockito.eq(HttpMethod.DELETE),
                        Matchers.<HttpEntity<String>>any(),
                        Matchers.<Class<Resource>>any());

        deleteService.deleteFile(fileUrl, "AAAABBBB", "12344");

        assertFalse("Failed to receive exception resulting from non-running EM service", true);
    }


    /**
     * This method sets up the mock evidence management document service endpoint for the currently executing test.
     * <p/>
     *
     * @param fileUrl    a String containing the url for which the mock endpoint will respond
     * @param httpStatus an HttpStatus enum representing the http status value to be returned from the mock endpoint
     */

    private void setupMockEvidenceManagementService(String fileUrl,
                                                    HttpStatus httpStatus) {
        when(authTokenGenerator.generate()).thenReturn("xxxx");

        doReturn(new ResponseEntity<>(httpStatus))
                .when(restTemplate)
                .exchange(Mockito.eq(fileUrl),
                        Mockito.eq(HttpMethod.DELETE),
                        Matchers.<HttpEntity<String>>any(),
                        Matchers.<Class<Resource>>any());
    }
}