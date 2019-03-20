package uk.gov.hmcts.reform.emclient.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceImplUTest {

    private static  final String TEST_FILE_ID = "testFileId";
    private static  final String TEST_AUTH_TOKEN = "testAuthToken";
    private static  final String TEST_REQUEST_ID = "testRequestId";
    private static  final String DM_STORE_URL = "http://dmstore.url";
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
    public void setUp(){
        ReflectionTestUtils.setField(classToTest, "evidenceManagementStoreUrl", DM_STORE_URL);
    }

    @Test
    public void givenDocumentId_whenDownloadFile_thenDMStoreServiceIsCalled(){
        ResponseEntity<byte[]> expectedResponse = mock(ResponseEntity.class);
        String url = DM_STORE_URL+"/"+TEST_FILE_ID+"/binary";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(expectedResponse);
        ResponseEntity<byte[]> response = classToTest.downloadFile(TEST_FILE_ID, TEST_AUTH_TOKEN, TEST_REQUEST_ID);
        assertEquals(expectedResponse, response);
    }


}
