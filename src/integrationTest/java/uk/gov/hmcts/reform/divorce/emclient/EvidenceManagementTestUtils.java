package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.utils.IDAMUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvidenceManagementTestUtils {


    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAuthenticationTokenHeader(String username, String password,
                                                            IDAMUtils idamTestSupportUtil) {
        idamTestSupportUtil.createUserInIdam(username, password);
        String authenticationToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER_NAME, authenticationToken);
        return headers;
    }


    /**
     * Given the uri it will update the url to corresponding localhost url for testing with docker
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
     * Make REST call to an emclient API with user token to store a document in EM Store
     * <p/>
     *
     * @param filePath the name of the file to be sent to the EM store
     * @param fileContentType the fileContentType represents the contentType of the file
     * @return the id of the stored document
     */
    @SuppressWarnings("unchecked")
    public String uploadFileToEvidenceManagement(String filePath,
                                                 String fileContentType, String username,
                                                 String password, String evidenceManagementClientApiBaseUrl,
                                                 String documentManagementURL, IDAMUtils idamTestSupportUtil) {

        File file = new File(filePath);
        Response response = SerenityRest.given()
                .headers(getAuthenticationTokenHeader(username, password, idamTestSupportUtil))
                .multiPart("file", file, fileContentType)
                .post(evidenceManagementClientApiBaseUrl.concat("/upload"))
                .andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());
        return getDocumentStoreURI(((List<String>) response.getBody().path("fileUrl")).get(0), documentManagementURL);
    }
}
