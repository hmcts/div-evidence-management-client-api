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

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.emclient.EvidenceManagementTestUtils.AUTHORIZATION_HEADER_NAME;


@Lazy
@RunWith(SerenityRunner.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce.emclient", "uk.gov.hmcts.auth.provider.service"})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
        FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class EvidenceManagementFileDeleteIntegrationTest {

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Autowired
    private EvidenceManagementTestUtils evidenceManagementTestUtils;

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementURL;

    private static final String FILE_PATH = "src/integrationTest/resources/FileTypes/PNGFile.png";
    private static final String IMAGE_FILE_CONTENT_TYPE = "image/png";
    public static final String DELE_ENDPOINT = "/deleteFile?fileUrl=";


    @Test
    public void verifyDeleteRequestForExistingDocumentIsSuccessful() {
        String fileUrl = uploadFile();
        Response response = deleteFileFromEvidenceManagement(fileUrl, evidenceManagementTestUtils.getAuthenticationTokenHeader());
        Assert.assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestForNonExistentDocumentIs404NotFound() {
        String fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.concat("xyzzy");
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, evidenceManagementTestUtils.getAuthenticationTokenHeader());

        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithMissingDocumentIdIsNotAllowed() {
        String fileUrl = uploadFile();
        String fileUrlAlt = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
        Response response = deleteFileFromEvidenceManagement(fileUrlAlt, evidenceManagementTestUtils.getAuthenticationTokenHeader());

        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithInvalidAuthTokenIsForbidden() {
        String fileUrl = uploadFile();
        Map<String, Object> headers = evidenceManagementTestUtils.getAuthenticationTokenHeader();
        String token = "x".concat(headers.get(AUTHORIZATION_HEADER_NAME).toString()).concat("x");
        headers.put(AUTHORIZATION_HEADER_NAME, token);
        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);

        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }


    @Test
    public void verifyDeleteRequestWithUnauthorisedAuthTokenIsForbidden() {
        String fileUrl = uploadFile();
        Map<String, Object> headers = new HashMap<>();
        String invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkw" +
            "IiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImp0aSI6ImU4YjIwY2UxLTE3NGYtNDQ2My0" +
            "4N2U5LWQxNGJmZTA0MmViZiIsImlhdCI6MTUzMDE4MjYwNSwiZXhwIjoxNTMwMTg2MjA1fQ.7NTzU-S" +
            "cjcIcZ4bI8XkJkVZiFBHSwgVnW1FrcKZL1H8";
        headers.put(AUTHORIZATION_HEADER_NAME, String.format("Bearer %s", invalidToken));

        Response response = deleteFileFromEvidenceManagement(fileUrl, headers);
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }

    private Response deleteFileFromEvidenceManagement(String fileUrl, Map<String, Object> headers) {
        return SerenityRest.given()
                .headers(headers)
                .delete(evidenceManagementClientApiBaseUrl.concat(DELE_ENDPOINT + fileUrl))
                .andReturn();
    }

    private String uploadFile(){
        return evidenceManagementTestUtils.uploadFileToEvidenceManagement(FILE_PATH, IMAGE_FILE_CONTENT_TYPE,
                                                                        evidenceManagementClientApiBaseUrl,
                                                                        documentManagementURL);

    }
}