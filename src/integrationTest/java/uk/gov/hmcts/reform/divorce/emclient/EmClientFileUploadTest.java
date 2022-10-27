package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.junit.annotations.TestData;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.junit.Assert.assertEquals;

@RunWith(SerenityParameterizedRunner.class)
public class EmClientFileUploadTest extends IntegrationTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${evidence.management.client.api.endpoint.uploadwiths2stoken}")
    private String emClientApiUploadEndpoint;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private final String name;
    private final String fileType;

    private static String[] fileName = {"PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg",
        "PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg"};

    private static String[] fileContentType = {"image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg",
        "image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg"};

    @TestData
    public static Collection<Object[]> testData() {
        return IntStream.range(0, fileName.length)
                .mapToObj(i -> new String[]{fileName[i], fileContentType[i]})
                .collect(Collectors.toList());
    }

    public EmClientFileUploadTest(String filename, String fileContentType) {
        this.name = filename;
        this.fileType = fileContentType;
    }

    @Test
    @Ignore
    public void uploadFile() {
        uploadFileToEmStore(this.name, this.fileType);
    }

    @SuppressWarnings("unchecked")
    private void uploadFileToEmStore(String fileToUpload, String fileContentType) {
        File file = new File("src/integrationTest/resources/FileTypes/" + fileToUpload);
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader())
            .multiPart("file", file, fileContentType)
            .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
            .andReturn();

        String fileUrl = ((List<String>) response.getBody().path("fileUrl")).get(0);

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertEmGetFileResponse(fileToUpload, fileContentType, fileUrl);
    }

    private void assertEmGetFileResponse(String fileToUpload, String fileContentType, String fileUrl) {
        Response responseFromEvidenceManagement = readDataFromEvidenceManagement(fileUrl);

        assertEquals(HttpStatus.OK.value(), responseFromEvidenceManagement.getStatusCode());
        assertEquals(fileToUpload, responseFromEvidenceManagement.getBody().path("originalDocumentName"));
        assertEquals(fileContentType, responseFromEvidenceManagement.getBody().path("mimeType"));
    }

    public Response readDataFromEvidenceManagement(String uri) {
        String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        String password = UUID.randomUUID().toString().toUpperCase(Locale.UK);
        idamTestSupportUtil.createCaseworkerUserInIdam(username, password);
        Map<String, Object> headers = new HashMap<>();
        headers.put("ServiceAuthorization", authTokenGenerator.generate());
        headers.put("user-id", username);
        headers.put("user-roles", "caseworker-divorce");

        return given()
            .contentType("application/json")
            .headers(headers)
            .when()
            .get(uri)
            .andReturn();
    }

    private Map<String, Object> getAuthenticationTokenHeader() {
        String authenticationToken = idamTestSupportUtil.getIdamTestUser();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", authenticationToken);
        headers.put("Content-Type", "multipart/form-data");
        return headers;
    }
}
