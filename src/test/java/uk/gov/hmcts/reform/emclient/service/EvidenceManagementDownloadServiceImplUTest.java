package uk.gov.hmcts.reform.emclient.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceImplUTest {

    private static  final String TEST_FILE_ID = "testFileId";
    private static  final String TEST_AUTH_TOKEN = "testAuthToken";
    private static  final String DM_STORE_URL = "http://dmstore.url";
    private static  final String SERVICE_TOKEN = "serviceToken";
    private static  final String EM_STORE_URL = "evidenceManagementStoreUrl";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementDownloadServiceImpl classToTest;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(classToTest, EM_STORE_URL, DM_STORE_URL);
    }

    @Test
    public void givenFile_whenDownloadFile_thenDmStoreServiceIsCalled() {
        ResponseEntity<byte[]> expectedResponse = mock(ResponseEntity.class);

        when(userService.getUserDetails(TEST_AUTH_TOKEN)).thenReturn(UserDetails.builder().build());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        String url = DM_STORE_URL + "/" + TEST_FILE_ID + "/binary";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(expectedResponse);
        ResponseEntity<byte[]> response = classToTest.downloadFile(TEST_FILE_ID, TEST_AUTH_TOKEN);

        assertEquals(expectedResponse, response);
    }
}
