package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.emclient.service.UploadRequestBuilder.prepareRequest;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementUploadServiceImplTest {
    private static final String AUTHORIZATION_TOKEN = "AAAAA";
    private static final String REQUEST_ID = "123333";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final MockMultipartFile MOCK_MULTIPART_FILE = mockMultipartFile();
    private static final List<MultipartFile> MOCK_MULTIPART_FILE_LIST
            = Collections.singletonList(MOCK_MULTIPART_FILE);
    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "evidenceManagementServiceURL";
    private static final String EVIDENCE_MANAGEMENT_STORE_URL = "evidenceManagementStoreUrl";

    @InjectMocks
    private EvidenceManagementUploadServiceImpl classUnderTest = new EvidenceManagementUploadServiceImpl();

    @Mock
    private RestTemplate restTemplate;

    private static MockMultipartFile mockMultipartFile() {
        return new MockMultipartFile("file", "JDP.pdf", "application/pdf",
                "This is a test pdf file".getBytes());
    }

    @Before
    public void before() {
        ReflectionTestUtils.setField(classUnderTest, "evidenceManagementServiceURL", EVIDENCE_MANAGEMENT_SERVICE_URL);
        ReflectionTestUtils.setField(classUnderTest, "evidenceManagementStoreUrl", EVIDENCE_MANAGEMENT_STORE_URL);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldUploadFileWithUserTokenAndReturnFileUrlWithMetadataWhenValidInputsArePassed() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity, ObjectNode.class))
                .thenReturn(objectNode);

        assertThat(classUnderTest.uploadFilesWithUserAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID),
                contains(allOf(hasProperty("fileUrl", containsString("http://localhost:8080/documents/6")),
                        hasProperty("fileName", containsString("JDP.pdf")),
                        hasProperty("createdBy", containsString("testuser")),
                        hasProperty("createdOn", containsString("2017-09-01T13:12:36.862+0000")),
                        hasProperty("lastModifiedBy", containsString("testuser")),
                        hasProperty("modifiedOn", containsString("2017-09-01T13:12:36.860+0000")),
                        hasProperty("mimeType", containsString(MediaType.APPLICATION_PDF_VALUE)),
                        hasProperty("status", is(HttpStatus.OK)))));

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity,
                ObjectNode.class);
    }

    @Test(expected = ResourceAccessException.class)
    public void shouldNotUploadFileWithUserTokenAndThrowExceptionWhenEMServiceIsNotAvailable() {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity,
                ObjectNode.class)).thenThrow(new ResourceAccessException("Not able to connect to EM Service"));

        classUnderTest.uploadFilesWithUserAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID);

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity,
                ObjectNode.class);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotUploadFileWithUserTokenAndThrowExceptionWhenAuthorizationTokenIsInvalid() {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity,
                ObjectNode.class)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        classUnderTest.uploadFilesWithUserAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID);

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_SERVICE_URL, httpEntity,
                ObjectNode.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldUploadFileWithS2STokenAndReturnFileUrlWithMetadataWhenValidInputsArePassed() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(SERVICE_AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class)).thenReturn(objectNode);

        assertThat(classUnderTest.uploadFilesWithS2SAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID),
                contains(allOf(hasProperty("fileUrl", containsString("http://localhost:8080/documents/6")),
                        hasProperty("fileName", containsString("JDP.pdf")),
                        hasProperty("createdBy", containsString("testuser")),
                        hasProperty("createdOn", containsString("2017-09-01T13:12:36.862+0000")),
                        hasProperty("lastModifiedBy", containsString("testuser")),
                        hasProperty("modifiedOn", containsString("2017-09-01T13:12:36.860+0000")),
                        hasProperty("mimeType", containsString(MediaType.APPLICATION_PDF_VALUE)),
                        hasProperty("status", is(HttpStatus.OK)))));

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class);
    }

    @Test(expected = ResourceAccessException.class)
    public void shouldNotUploadFileWithS2STokenAndThrowExceptionWhenEMStoreIsNotAvailable() {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(SERVICE_AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class)).thenThrow(new ResourceAccessException("Not able to connect to EM Store"));

        classUnderTest.uploadFilesWithS2SAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID);

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotUploadFileWithS2STokenAndThrowExceptionWhenAuthorizationTokenIsInvalid() {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(SERVICE_AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        classUnderTest.uploadFilesWithS2SAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID);

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldUploadFileWithS2STokenAndReturnNullForEmptyFieldsWhenValidInputsArePassed() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponsewithnullvalues.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpEntity(SERVICE_AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, MOCK_MULTIPART_FILE_LIST);

        when(restTemplate.postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class)).thenReturn(objectNode);

        assertThat(classUnderTest.uploadFilesWithS2SAuthToken(MOCK_MULTIPART_FILE_LIST, AUTHORIZATION_TOKEN, REQUEST_ID),
                contains(allOf(hasProperty("fileUrl", containsString("http://localhost:8080/documents/6")),
                        hasProperty("fileName", containsString("JDP.pdf")),
                        hasProperty("createdBy", is(nullValue())),
                        hasProperty("createdOn", containsString("2017-09-01T13:12:36.862+0000")),
                        hasProperty("lastModifiedBy", is(nullValue())),
                        hasProperty("modifiedOn", is(nullValue())),
                        hasProperty("mimeType", containsString(MediaType.APPLICATION_PDF_VALUE)),
                        hasProperty("status", is(HttpStatus.OK)))));

        verify(restTemplate, times(1)).postForObject(EVIDENCE_MANAGEMENT_STORE_URL, httpEntity,
                ObjectNode.class);
    }

    @Test
    public void givenStoredFileIsNull_whenGetTextFromJsonNode_thenReturnNull(){
        assertNull(getTextFromJsonNode(null, "someText"));
    }

    @Test
    public void givenNotTextIsNull_whenGetTextFromJsonNode_thenReturnNull() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        assertNull(getTextFromJsonNode(objectNode, null));
    }

    @Test
    public void givenNotTextIsBlank_whenGetTextFromJsonNode_thenReturnNull() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        assertNull(getTextFromJsonNode(objectNode, "  "));
    }

    @Test
    public void givenDataIsNotPresent_whenGetTextFromJsonNode_thenReturnNull() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        final JsonNode jsonNode = objectNode.get("_embedded").get("documents").get(0);

        assertNull(getTextFromJsonNode(jsonNode, "nonExistentData"));
    }

    @Test
    public void givenDataPresent_whenGetTextFromJsonNode_thenReturnData() throws Exception {
        final String hatoesResponse = new String(readAllBytes(get("src/test/resources/fileuploadresponse.txt")));
        final ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(hatoesResponse);

        final JsonNode jsonNode = objectNode.get("_embedded").get("documents").get(0);

        String actual = getTextFromJsonNode(jsonNode, "mimeType");

        assertEquals("application/pdf", actual);
    }

    private String getTextFromJsonNode(JsonNode storedFile, String node){
        return ReflectionTestUtils.invokeMethod(classUnderTest, "getTextFromJsonNode", storedFile, node);
    }

    private HttpEntity<MultiValueMap<String, Object>> getHttpEntity(String authHeaderName, String authorizationToken, List<MultipartFile> files) {
        MultiValueMap<String, Object> parameters = prepareRequest(files);

        HttpHeaders headers = new HttpHeaders();
        headers.add(authHeaderName, authorizationToken);
        headers.set("Content-Type", "multipart/form-data");

        return new HttpEntity<>(parameters, headers);
    }
}