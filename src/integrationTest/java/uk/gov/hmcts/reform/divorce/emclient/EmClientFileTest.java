package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.junit.annotations.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

@RunWith(SerenityParameterizedRunner.class)
@Slf4j
public class EmClientFileTest extends IntegrationTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    private String username;
    private String password;
    private boolean userInitialized;

    private final String name;
    private final String fileType;
    private static final int DOC_UUID_LENGTH = 36;


    private static String[] fileName = {"PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg",
        "PNGFile.png", "BMPFile.bmp", "PDFFile.pdf", "TIFFile.TIF", "JPEGFile.jpg"};

    private static String[] fileContentType = {"image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg",
        "image/png", "image/bmp", "application/pdf", "image/tiff", "image/jpeg"};

    @TestData
    public static Collection<Object[]> testData() {
        return IntStream.range(0, fileName.length)
            .mapToObj(i -> new String[] {fileName[i], fileContentType[i]})
            .collect(Collectors.toList());
    }

    public EmClientFileTest(String filename, String fileContentType) {
        this.name = filename;
        this.fileType = fileContentType;
    }

    @Before
    public void setUpTest() {
        if (!userInitialized) {
            username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
            password = UUID.randomUUID().toString().toUpperCase(Locale.UK);
            idamTestSupportUtil.createCaseworkerUserInIdam(username, password);
            log.info("Set up Caseworker with username {} and pwd {}", username, password);
            userInitialized = true;
        }
    }

    @Test
    public void uploadFile() throws Exception {
        uploadFileToEmStore(this.name, this.fileType);
    }

    @SuppressWarnings("unchecked")
    private void uploadFileToEmStore(String fileToUpload, String fileContentType) throws Exception {
        File file = new File("src/integrationTest/resources/FileTypes/" + fileToUpload);
        String fileUrl = uploadFileTest(fileContentType, file);
        downloadFileTest(fileUrl, file, fileContentType);
        deleteFileTest(fileUrl, fileContentType);
    }

    private String uploadFileTest(String fileContentType, File file) {
        log.info("File upload test with filename {}", file.getName());
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader())
            .multiPart("file", file, fileContentType)
            .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
            .andReturn();
        log.info("File upload response received with status {}", response.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response.statusCode());
        String fileUrl = ((List<String>) response.getBody().path("fileUrl")).get(0);
        log.info("File url {}", fileUrl);
        return fileUrl;
    }

    private void downloadFileTest(String fileUrl, File file, String fileContentType) throws Exception {
        UUID documentId = getDocumentIdFromSelfHref(URI.create(fileUrl).getPath().replaceFirst("/", ""));
        log.info("File download test with documentId {}", documentId);
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader())
            .multiPart("file", file,fileContentType)
            .get(evidenceManagementClientApiBaseUrl.concat("/download/" + documentId.toString()))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        byte[] actualContent = response.getBody().asByteArray();
        byte[] expectedContent = Files.readAllBytes(file.toPath());
        assertThat(actualContent, equalTo(expectedContent));
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

    private void deleteFileTest(String fileUrl, String fileContentType) {
        Response response = SerenityRest.given()
            .headers(getAuthenticationTokenHeader())
            .multiPart("file", fileContentType)
            .param("fileUrl", fileUrl)
            .delete(evidenceManagementClientApiBaseUrl.concat("/deleteFile"))
            .andReturn();
        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }


    private Map<String, Object> getAuthenticationTokenHeader() {
        String authenticationToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", authenticationToken);
        headers.put("Content-Type", "multipart/form-data");
        return headers;
    }
}
