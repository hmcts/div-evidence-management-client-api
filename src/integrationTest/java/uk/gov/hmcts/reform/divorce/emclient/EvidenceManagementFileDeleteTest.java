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
import java.util.ArrayList;
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
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";

    private static final String CITIZEN_USERNAME = "CitizenTestUser";
    private static final String CITIZEN_EMAIL = "CitizenTestUser@test.com";
    private static final String PASSWORD = "password";
    private static final String SERVICE_AUTHORIZATION_HEADER_NAME = "ServiceAuthorization";


    @Test
    public void verifyDeleteRequestForExistingDocumentIsSuccessful() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, IMAGE_FILE_CONTENT_TYPE);
        Response response = deleteFileFromEvidenceManagement(fileUrl, getAuthenticationTokenHeader(false));

        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestForNonExistentDocumentIs404NotFound() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, IMAGE_FILE_CONTENT_TYPE);
        String fileUrlAlt = fileUrl.concat("xyzzy");
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, getAuthenticationTokenHeader(false));

        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithMissingDocumentIdIs405NotAllowed() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, IMAGE_FILE_CONTENT_TYPE);
        String fileUrlAlt = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, getAuthenticationTokenHeader( false));

        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithInvalidAuthTokenIsRejected() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, IMAGE_FILE_CONTENT_TYPE);
        Map<String, Object> headers = getAuthenticationTokenHeader( false);
        String token = "x".concat(headers.get(SERVICE_AUTHORIZATION_HEADER_NAME).toString()).concat("x");
        headers.put(SERVICE_AUTHORIZATION_HEADER_NAME, token);
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithUnauthorisedAuthTokenIsForbidden() {
        String fileUrl = uploadFileToEvidenceManagement(FILE_NAME, IMAGE_FILE_CONTENT_TYPE);

        Map<String, Object> headers = getAuthenticationTokenHeader(false);
        idamTestSupportUtil.createUserInIdam(CITIZEN_USERNAME, PASSWORD);
        String token = idamTestSupportUtil.generateUserTokenWithNoRoles(CITIZEN_USERNAME, PASSWORD);
        headers.put(SERVICE_AUTHORIZATION_HEADER_NAME, token);

        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
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
        Response response = SerenityRest.given()
                .headers(getAuthenticationTokenHeader( true))
                .multiPart("files", file, fileContentType)
                .multiPart("classification", "PRIVATE")
                .post("http://localhost:4603/documents")
                .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());
        String url = (String) ((ArrayList) response.getBody().path("_embedded.documents._links.self.href")).get(0);

        return getDocumentStoreURI(url);
    }


    private Response deleteFileFromEvidenceManagement(String fileUrl, Map<String, Object> headers) {
        return SerenityRest.given()
                .headers(headers)
                .delete(fileUrl)
                .andReturn();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAuthenticationTokenHeader(Boolean upload) {
        String authenticationToken = authTokenGenerator.generate();
        Map<String, Object> headers = new HashMap<>();
        headers.put(SERVICE_AUTHORIZATION_HEADER_NAME, authenticationToken);
        if(upload != null && upload){
            headers.put("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        }
        headers.put("user-id", CITIZEN_EMAIL);
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