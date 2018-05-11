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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Lazy
@RunWith(SerenityRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
        FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EvidenceManagementFileDeleteTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Autowired
    private IDAMUtils idamTestSupportUtil;

    @Value("${evidence.management.client.api.endpoint.uploadwiths2stoken}")
    private String emClientApiUploadEndpoint;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementURL;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String FILE_NAME = "PNGFile.png";


    @Test
    public void verifyDeleteRequestForExistingDocumentIsSuccessful() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, "image/png");
        System.out.println("fileUrl: " + fileUrl);
        Response response = deleteFileFromEvidenceManagement(fileUrl, getAuthenticationTokenHeader("CitizenTestUser", "password", false));

        Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestForNonExistentDocumentIsIgnored() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, "image/png");
        String fileUrlAlt = fileUrl.concat("xyzzy");
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, getAuthenticationTokenHeader("CitizenTestUser", "password", false));

        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithMissingDocumentIdIsIgnored() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, "image/png");
        String fileUrlAlt = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, getAuthenticationTokenHeader("CitizenTestUser", "password", false));

        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithInvalidAuthTokenIsRejected() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, "image/png");
        Map<String, Object> headers = getAuthenticationTokenHeader("CitizenTestUser", "password", false);
        String token = "x".concat(headers.get("ServiceAuthorization").toString()).concat("x");
        headers.put("ServiceAuthorization", token);
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithUnauthorisedAuthTokenIsRejected() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, "image/png");
        Response response = deleteFileFromEvidenceManagement(fileUrl, getAuthenticationTokenHeader("CaseWorkerTest", "password", false));

        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
    }

    /**
     * Make REST call to an emclient API with user token to store a document in EM Store
     * <p/>
     *
     * @param fileName        the name of the file to be sent to the EM store
     * @param fileContentType the fileContentType represents the contentType of the file
     * @return the id of the stored document
     */
    @SuppressWarnings("unchecked")
    private String uploadFileToEvidenceManagement(String fileName,
                                          String fileContentType) {

        File file = new File("../../src/integrationTest/resources/FileTypes/" + fileName);
        System.out.println("uploadEndpoint: " + evidenceManagementClientApiBaseUrl.concat("/upload"));
        Response response = SerenityRest.given()
                .headers(getAuthenticationTokenHeader("CitizenTestUser", "password", true))
                .multiPart("file", file, fileContentType)
                .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
                .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());

        return getDocumentStoreURI(((List<String>) response.getBody().path("fileUrl")).get(0));
    }


    private Response deleteFileFromEvidenceManagement(String fileUrl, Map<String, Object> headers) {
        String documentId = fileUrl.substring(fileUrl.lastIndexOf('/'));
        //fileUrl = fileUrl + headers.get("ServiceAuthorization");
        System.out.println("deleteEndpoint = fileUrl: " + fileUrl);
//        System.out.println("deleteEndpoint changed to: " + "http://localhost:4603/emclientapi/version/1/documents" + documentId);

        return SerenityRest.given()
                .headers(headers)
//                .delete("http://localhost:4603/emclientapi/version/1/documents" + documentId)
//                .delete(evidenceManagementClientApiBaseUrl.concat("/documents" + documentId))
                .delete(fileUrl)
                .andReturn();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAuthenticationTokenHeader(String username, String password, Boolean upload) {
        idamTestSupportUtil.createUserInIdam(username, password);
//        String authenticationToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);
        String authenticationToken = authTokenGenerator.generate();
        System.out.println("authenticationToken before: " + authenticationToken);
        authenticationToken = authenticationToken.substring(authenticationToken.indexOf(' ') + 1);
        System.out.println("authenticationToken after: " + authenticationToken);
        Map<String, Object> headers = new HashMap<>();
        headers.put("ServiceAuthorization", authenticationToken);
        headers.put("Content-Type", "multipart/form-data");
        headers.put("user-id", "CitizenTestUser@test.com");
        headers.put("Accept", "*/*");
        return headers;
    }

    /**
     * Given the uri it will update the url to corresponding localhost url for testing with docker
     *
     * @param uri the link to be updated
     * @return updated url
     */
    //this is a hack to make this work with the docker container
    String getDocumentStoreURI(String uri) {
        if (uri.contains("http://em-api-gateway-web:3404")) {
            return uri.replace("http://em-api-gateway-web:3404", documentManagementURL);
        }

        if (uri.contains("document-management-store:8080")) {
            return uri.replace("http://document-management-store:8080", documentManagementURL);
        }

        return uri;
    }

}