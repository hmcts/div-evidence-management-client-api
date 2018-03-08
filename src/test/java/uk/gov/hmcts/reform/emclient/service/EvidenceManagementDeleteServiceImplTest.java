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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDeleteServiceImplTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "evidenceManagementServiceURL";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EvidenceManagementDeleteServiceImpl deleteService = new EvidenceManagementDeleteServiceImpl();



    @Before
    public void before() {
        ReflectionTestUtils.setField(deleteService, "evidenceManagementServiceURL", EVIDENCE_MANAGEMENT_SERVICE_URL);
    }


    @Test
    public void shouldPassThruDocumentDeletedSuccessfullyState() {

        setupMockEvidenceManagementService(HttpStatus.OK);

        ResponseEntity<String> response = deleteService.deleteDocument("56", "AAAABBBB", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void shouldPassThruNoDocumentIdIsPassedState() {

        setupMockEvidenceManagementService(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = deleteService.deleteDocument("", "AAAABBBB", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }


    @Test
    public void shouldPassThruNotAuthorisedAuthTokenState() {

        setupMockEvidenceManagementService(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> response = deleteService.deleteDocument("56", "CCCCDDDD", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    public void shouldPassThruNotAuthenticatedAuthTokenState() {

        setupMockEvidenceManagementService(HttpStatus.FORBIDDEN);

        ResponseEntity<String> response = deleteService.deleteDocument("56", "", "12344");

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {

        doThrow(ResourceAccessException.class)
                .when(restTemplate)
                .exchange(Mockito.eq(EVIDENCE_MANAGEMENT_SERVICE_URL),
                          Mockito.eq(HttpMethod.DELETE),
                          Matchers.<HttpEntity<String>> any(),
                          Matchers.<Class<Resource>> any(),
                          Mockito.any(String.class));

        deleteService.deleteDocument("25", "AAAABBBB", "12344");

        assertFalse("Failed to receive exception resulting from non-running EM service", true);
    }


    private void setupMockEvidenceManagementService(HttpStatus httpStatus) {

        doReturn(new ResponseEntity<>(httpStatus))
                .when(restTemplate)
                .exchange(Mockito.eq(EVIDENCE_MANAGEMENT_SERVICE_URL),
                          Mockito.eq(HttpMethod.DELETE),
                          Matchers.<HttpEntity<String>> any(),
                          Matchers.<Class<Resource>> any(),
                          Mockito.any(String.class));
    }
}
