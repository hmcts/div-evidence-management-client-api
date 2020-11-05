package uk.gov.hmcts.reform.divorce.emclient;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDeleteServiceImpl;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementDownloadServiceImpl;
import uk.gov.hmcts.reform.emclient.service.EvidenceManagementUploadServiceImpl;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = EvidenceManagementClientApplication.class)
@PactTestFor(providerName = "em_dm_store", port = "3404")
@SpringBootTest({
        "evidence.management.store.upload.file.url : http://localhost:3404/documents"
})
public class EmClientConsumerTest {
    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String REQ_ID = "ReqId";
    private static final String DOCUMENT_ID = "5c3c3906-2b51-468e-8cbb-a4002eded075";


    @Autowired
    private EvidenceManagementUploadServiceImpl emUploadService;

    @Autowired
    private EvidenceManagementDeleteServiceImpl emDeleteService;

    @Autowired
    private EvidenceManagementDownloadServiceImpl emDownloadService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Value("${evidence.management.store.upload.file.url}")
    private String documentManagementUrl;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(consumer = "divorce_emClient")
    RequestResponsePact deleteDocument(PactDslWithProvider builder) throws IOException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("ServiceAuthorization", SOME_SERVICE_AUTHORIZATION_TOKEN);
        headers.put("user-id", "id1");

        return builder
                .given("I have existing document")
                .uponReceiving("a request for delete the document")
                .path("/documents/" + DOCUMENT_ID)
                .method("DELETE")
                .headers(headers)
                .willRespondWith()
                .status(204)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteDocument")
    public void verifyDeleteDocumentPact() throws JSONException {
        UserDetails userDetails = getUserDetails();
        when(userService.getUserDetails(SOME_AUTHORIZATION_TOKEN)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SOME_SERVICE_AUTHORIZATION_TOKEN);
        ResponseEntity<?> responses = emDeleteService.deleteFile(documentManagementUrl + "/" + DOCUMENT_ID,
                SOME_AUTHORIZATION_TOKEN, REQ_ID);
        assertThat(responses.getStatusCode().is2xxSuccessful());
    }

    @Pact(consumer = "divorce_emClient")
    RequestResponsePact downloadDocument(PactDslWithProvider builder) throws IOException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("ServiceAuthorization", SOME_SERVICE_AUTHORIZATION_TOKEN);
        headers.put("user-roles","caseworker-divorce-courtadmin");
        return builder
                .given("I have existing document")
                .uponReceiving("a request for download the document")
                .path("/documents/" + DOCUMENT_ID + "/binary")
                .method("GET")
                .headers(headers)
                .willRespondWith()
                .status(200)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "downloadDocument")
    public void verifyDownloadDocumentPact() throws IOException, JSONException {
        UserDetails userDetails = getUserDetails();
        when(userService.getUserDetails(SOME_AUTHORIZATION_TOKEN)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SOME_SERVICE_AUTHORIZATION_TOKEN);
        ResponseEntity<?> responses = emDownloadService.downloadFile(DOCUMENT_ID,
                SOME_AUTHORIZATION_TOKEN);
        assertTrue(responses.getStatusCode().is2xxSuccessful());
    }

    private UserDetails getUserDetails() {
        UserDetails userDetails = UserDetails.builder().id("id1")
                .email("one@email.com")
                .forename("two")
                .surname("one")
                .build();
        return userDetails;
    }
}




