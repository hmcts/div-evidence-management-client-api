package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.junit.annotations.TestData;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.serenitybdd.rest.SerenityRest.given;

@Lazy
@RunWith(SerenityParameterizedRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
        FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EMClientFileUploadTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${evidence.management.client.api.endpoint.uploadwiths2stoken}")
    private String emClientApiUploadEndpoint;

    @Autowired
    private IDAMUtils idamTestSupportUtil;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private String name;

    private String fileType;

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

    public EMClientFileUploadTest(String filename, String fileContentType) {
        this.name = filename;
        this.fileType= fileContentType;
    }

    @Test
    public void uploadFile() {
        uploadFileToEMStore(this.name, this.fileType);
    }

    @SuppressWarnings("unchecked")
    private void uploadFileToEMStore(String fileToUpload, String fileContentType) {
        File file = new File("src/integrationTest/resources/FileTypes/" + fileToUpload);
        System.out.println("David's super duper log" +
                "");
        System.out.println("File>"+file);
        Response response = SerenityRest.given()
                .headers(getAuthenticationTokenHeader("CitizenTestUser", "password"))
                .multiPart("file", file, fileContentType)
                .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
                .andReturn();

        System.out.println("Response>"+response);
        System.out.println("Response Pretty>"+response.getBody().prettyPrint());

        String fileUrl = ((List<String>) response.getBody().path("fileUrl")).get(0);
        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertEMGetFileResponse(fileToUpload, fileContentType, fileUrl);
    }

    private void assertEMGetFileResponse(String fileToUpload, String fileContentType, String fileUrl) {
        Response responseFromEvidenceManagement = readDataFromEvidenceManagement(fileUrl);
        Assert.assertEquals(HttpStatus.OK.value(), responseFromEvidenceManagement.getStatusCode());
        Assert.assertEquals(fileToUpload, responseFromEvidenceManagement.getBody().path("originalDocumentName"));
        Assert.assertEquals(fileContentType, responseFromEvidenceManagement.getBody().path("mimeType"));
    }

    public Response readDataFromEvidenceManagement(String uri) {
        idamTestSupportUtil.createDivorceCaseworkerUserInIdam("CaseWorkerTest", "password");
        Map<String, Object> headers = new HashMap<>();
        headers.put("ServiceAuthorization", authTokenGenerator.generate());
        headers.put("user-id", "CaseWorkerTest");
        headers.put("user-roles", "caseworker-divorce");
        return given()
                .contentType("application/json")
                .headers(headers)
                .when()
                .get(uri)
                .andReturn();
    }

    private Map<String, Object> getAuthenticationTokenHeader(String username, String password) {
        idamTestSupportUtil.createUserInIdam(username, password);
        String authenticationToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);
        Map<String, Object> headers = new HashMap<>();
        System.out.println("DAVID AUTH TOKEN"+authenticationToken);
        headers.put("Authorization", authenticationToken);
        headers.put("Content-Type", "multipart/form-data");
        return headers;
    }

}
