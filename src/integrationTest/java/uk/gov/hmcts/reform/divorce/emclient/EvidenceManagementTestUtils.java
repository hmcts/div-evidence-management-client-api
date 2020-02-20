package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvidenceManagementTestUtils {

    static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    Map<String, Object> getAuthenticationTokenHeader(IdamUtils idamTestSupportUtil) {
        String authenticationToken = idamTestSupportUtil.generateNewUserAndReturnToken();
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER_NAME, authenticationToken);
        return headers;
    }

    public Map<String, Object> getInvalidAuthenticationTokenHeader() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER_NAME, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
                + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
                + ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5cinvalid");
        return headers;
    }

    /**
     * Given the uri it will update the url to corresponding localhost url for testing with docker.
     *
     * @param uri the link to be updated
     * @return updated url
     */
    //this is a hack to make this work with the docker container
    String getDocumentStoreURI(String uri, String documentManagementURL) {
        if (uri.contains("http://em-api-gateway-web:3404")) {
            return uri.replace("http://em-api-gateway-web:3404", documentManagementURL);
        }

        if (uri.contains("document-management-store:8080")) {
            return uri.replace("http://document-management-store:8080", documentManagementURL);
        }

        return uri;
    }

    /**
     * Make REST call to an emclient API with user token to store a document in EM Store.
     * <p/>
     *
     * @param filePath the name of the file to be sent to the EM store
     * @param fileContentType the fileContentType represents the contentType of the file
     * @return the id of the stored document
     */
    @SuppressWarnings("unchecked")
    public String uploadFileToEvidenceManagement(String filePath,
                                                 String fileContentType,
                                                 String evidenceManagementClientApiBaseUrl,
                                                 String documentManagementURL,
                                                 IdamUtils idamTestSupportUtil) {
        File file = new File(filePath);
        Response response = SerenityRest.given()
                .headers(getAuthenticationTokenHeader(idamTestSupportUtil))
                .multiPart("file", file, fileContentType)
                .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
                .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());

        return getDocumentStoreURI(((List<String>) response.getBody().path("fileUrl")).get(0), documentManagementURL);
    }
}
