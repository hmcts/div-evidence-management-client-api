package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.emclient.EvidenceManagementTestUtils.AUTHORIZATION_HEADER_NAME;

@RunWith(SerenityRunner.class)
public class EvidenceManagementFileDeleteIntegrationTest extends IntegrationTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Autowired
    private IDAMUtils idamTestSupportUtil;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementURL;

    private EvidenceManagementTestUtils evidenceManagementTestUtils = new EvidenceManagementTestUtils();

    private static final String FILE_PATH = "src/integrationTest/resources/FileTypes/PNGFile.png";
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";
    private static final String DELETE_ENDPOINT = "/deleteFile?fileUrl=";

    @Test
    public void verifyDeleteRequestForExistingDocumentIsSuccessful() {
        String fileUrl = uploadFile();
        Response response = deleteFileFromEvidenceManagement(fileUrl, evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));
        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestForNonExistentDocumentIs404NotFound() {
        String fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.concat("xyzzy");
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));

        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithMissingDocumentIdIsNotAllowed() {
        String fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil));

        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithInvalidAuthTokenIsForbidden() {
        String fileUrl = uploadFile();
        Map<String, Object> headers = evidenceManagementTestUtils.getAuthenticationTokenHeader(idamTestSupportUtil);
        String token = "x".concat(headers.get(AUTHORIZATION_HEADER_NAME).toString()).concat("x");
        headers.put(AUTHORIZATION_HEADER_NAME, token);
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }

    @Test
    public void verifyDeleteRequestWithUnauthorisedAuthTokenIsForbidden() {
        String fileUrl = uploadFile();
        Map<String, Object> headers = evidenceManagementTestUtils.getInvalidAuthenticationTokenHeader();

        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }

    private Response deleteFileFromEvidenceManagement(String fileUrl, Map<String, Object> headers) {
        return SerenityRest.given()
                .headers(headers)
                .delete(evidenceManagementClientApiBaseUrl.concat(DELETE_ENDPOINT + fileUrl))
                .andReturn();
    }

    private String uploadFile(){
        return evidenceManagementTestUtils.uploadFileToEvidenceManagement(FILE_PATH, IMAGE_FILE_CONTENT_TYPE,
                                                                        evidenceManagementClientApiBaseUrl,
                                                                        documentManagementURL,
                                                                        idamTestSupportUtil);

    }
}