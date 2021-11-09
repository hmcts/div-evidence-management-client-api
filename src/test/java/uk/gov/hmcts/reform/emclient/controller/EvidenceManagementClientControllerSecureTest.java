package uk.gov.hmcts.reform.emclient.controller;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementSecureDocStoreService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EvidenceManagementClientController.class)
@ImportAutoConfiguration({
    RibbonAutoConfiguration.class,
    HttpMessageConvertersAutoConfiguration.class,
    FeignRibbonClientAutoConfiguration.class,
    FeignAutoConfiguration.class})
@ContextConfiguration(classes = EvidenceManagementClientApplication.class)
@TestPropertySource(properties = {"feature.secure-doc-store=true"})
public class EvidenceManagementClientControllerSecureTest {

    private static final String AUTH_TOKEN = "AAAAAAA";
    private static final String REQUEST_ID = "1234";
    private static final String AUTHORIZATION_TOKEN_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "requestId";
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private static final List<MultipartFile> MULTIPART_FILE_LIST = Collections.emptyList();
    private static final String INVALID_AUTH_TOKEN = "{[][][][][}";
    private static final String INVALID_FILE_ERROR_MSG =
        "Attempt to upload invalid file, this service only accepts the following file types ('jpg, jpeg, bmp, tif, tiff, png, pdf)";

    private static final String EM_CLIENT_UPLOAD_URL = "http://localhost/emclientapi/version/1/upload";
    private static final String EM_CLIENT_DELETE_ENDPOINT_URL = "/emclientapi/version/1/deleteFile?fileUrl=";
    private static final String UPLOADED_FILE_URL = "http://localhost:8080/documents/6";

    @MockBean
    private EvidenceManagementUploadService emUploadService;

    @MockBean
    private EvidenceManagementDeleteService emDeleteService;

    @MockBean
    private EvidenceManagementSecureDocStoreService emSecureDocService;

    @MockBean
    private ApacheHttpClientFactory apacheHttpClientFactory;

    @MockBean
    private ApacheHttpClientConnectionManagerFactory apacheHttpClientConnectionManagerFactory;

    @MockBean
    private HttpClientConnectionManager httpClientConnectionManager;

    @MockBean
    private CloseableHttpClient closeableHttpClient;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;
    private IdamTokens idamTokens;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        idamTokens = buildIdamTokens();
        given(userService.getIdamTokens(AUTH_TOKEN)).willReturn(idamTokens);

    }

    @Test
    public void shouldUploadFileTokenWhenHandleFileUploadIsInvokedWithValidInputs() throws Exception {
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willReturn(prepareFileUploadResponse());

        mockMvc.perform(multipart(EM_CLIENT_UPLOAD_URL)
            .file(jpegMultipartFile())
            .header(AUTHORIZATION_TOKEN_HEADER, AUTH_TOKEN)
            .header(REQUEST_ID_HEADER, REQUEST_ID)
            .header(CONTENT_TYPE_HEADER, MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].fileUrl", is("http://localhost:8080/documents/6")))
            .andExpect(jsonPath("$[0].fileName", is("test.txt")))
            .andExpect(jsonPath("$[0].createdBy", is("testuser")))
            .andExpect(jsonPath("$[0].createdOn", is("2017-09-01T13:12:36.862+0000")))
            .andExpect(jsonPath("$[0].lastModifiedBy", is("testuser")))
            .andExpect(jsonPath("$[0].modifiedOn", is("2017-09-01T13:12:36.862+0000")))
            .andExpect(jsonPath("$[0].mimeType", is(MediaType.TEXT_PLAIN_VALUE)))
            .andExpect(jsonPath("$[0].status", is("OK")));

        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }


    @Test
    public void shouldNotUploadFileAndThrowClientExceptionWhenHandleFileUploadWithS2STokenIsInvokedWithInvalidAuthToken()
        throws Exception {
        given(userService.getIdamTokens(INVALID_AUTH_TOKEN)).willReturn(idamTokens);
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
        mockMvc.perform(multipart(EM_CLIENT_UPLOAD_URL)
            .file(jpegMultipartFile())
            .header(AUTHORIZATION_TOKEN_HEADER, INVALID_AUTH_TOKEN)
            .header(REQUEST_ID_HEADER, REQUEST_ID)
            .header(CONTENT_TYPE_HEADER, MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().is4xxClientError());

        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }


    @Test
    public void shouldNotUploadFileAndThrowServerExceptionWhenHandleFileUploadWithS2STokenAndEmStoreThrowsHttpServerException()
        throws Exception {
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Not enough disk space available."));

        verifyExceptionFromUploadServiceIsHandledGracefully();

        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }

    @Test
    public void shouldNotUploadFileAndThrowServerExceptionWhenHandleFileIsInvokedAndEmServiceIsUnavailable()
        throws Exception {
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willThrow(new ResourceAccessException("Evidence management service is currently down"));

        verifyExceptionFromUploadServiceIsHandledGracefully();
        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }

    @Test
    public void shouldDeleteFileWhenDeleteFileIsInvokedWithFileUrl() throws Exception {
        mockMvc.perform(delete(EM_CLIENT_DELETE_ENDPOINT_URL + UPLOADED_FILE_URL)
            .header(AUTHORIZATION_TOKEN_HEADER, AUTH_TOKEN)
            .header(REQUEST_ID_HEADER, REQUEST_ID))
            .andExpect(status().isOk())
            .andReturn();
        verify(emSecureDocService).delete(UPLOADED_FILE_URL, idamTokens);
    }

    private IdamTokens buildIdamTokens() {
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamToken")
            .serviceAuthorization("serviceAuth")
            .build();
        return idamTokens;
    }

    private List<FileUploadResponse> prepareFileUploadResponse() {
        FileUploadResponse fileUploadResponse;
        fileUploadResponse = FileUploadResponse.builder().status(HttpStatus.OK)
            .fileUrl(UPLOADED_FILE_URL)
            .fileName("test.txt")
            .createdBy("testuser")
            .createdOn("2017-09-01T13:12:36.862+0000")
            .modifiedOn("2017-09-01T13:12:36.862+0000")
            .lastModifiedBy("testuser")
            .mimeType(MediaType.TEXT_PLAIN_VALUE).build();
        return Collections.singletonList(fileUploadResponse);
    }


    private MockMultipartFile jpegMultipartFile() {
        return new MockMultipartFile("image", "image.jpeg", "image/jpeg", new byte[0]);
    }

    private void verifyExceptionFromUploadServiceIsHandledGracefully() throws Exception {
        mockMvc.perform(multipart(EM_CLIENT_UPLOAD_URL)
            .file(jpegMultipartFile())
            .header(AUTHORIZATION_TOKEN_HEADER, AUTH_TOKEN)
            .header(REQUEST_ID_HEADER, REQUEST_ID)
            .header(CONTENT_TYPE_HEADER, MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().is5xxServerError())
            .andExpect(content().string(
                "Some server side exception occurred. Please check logs for details"));
    }
}
