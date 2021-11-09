package uk.gov.hmcts.reform.emclient.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {"feature.secure-doc-store=true"})
public class EvidenceManagementClientControllerSecureTest extends EvidenceManagementClientControllerTest {

    @MockBean
    private UserService userService;
    private IdamTokens idamTokens;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        idamTokens = buildIdamTokens();
        given(userService.getIdamTokens(AUTH_TOKEN)).willReturn(idamTokens);
    }

    @Test
    @Override
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
    @Override
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
    @Override
    public void shouldNotUploadFileAndThrowServerExceptionWhenHandleFileUploadWithS2STokenAndEmStoreThrowsHttpServerException()
        throws Exception {
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Not enough disk space available."));

        verifyExceptionFromUploadServiceIsHandledGracefully();

        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }

    @Test
    @Override
    public void shouldNotUploadFileAndThrowServerExceptionWhenHandleFileIsInvokedAndEmServiceIsUnavailable()
        throws Exception {
        given(emSecureDocService.upload(MULTIPART_FILE_LIST, idamTokens))
            .willThrow(new ResourceAccessException("Evidence management service is currently down"));

        verifyExceptionFromUploadServiceIsHandledGracefully();
        verify(emSecureDocService).upload(MULTIPART_FILE_LIST, idamTokens);
    }


    private IdamTokens buildIdamTokens() {
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamToken")
            .serviceAuthorization("serviceAuth")
            .build();
        return idamTokens;
    }
}
