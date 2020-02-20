package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SerenityRunner.class)
public class EMClientFileDownloadTest extends IntegrationTest {

    private static final String FILE_TO_DOWNLOAD = "ce377eb6-baa8-4b80-b183-e9f90a71ccf9";
    private static final String TEST_FILE = "documents/testDocument.pdf";

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Test
    public void downloadFileTest()throws Exception {
        Response response = SerenityRest.given()
                .headers(getDownloadAuthenticationTokenHeader())
                .get(evidenceManagementClientApiBaseUrl + "/download/" + FILE_TO_DOWNLOAD)
                .andReturn();

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        byte[] actualContent = response.getBody().asByteArray();
        byte[] expectedContent = ResourceLoader.loadResource(TEST_FILE);
        assertThat(actualContent, equalTo(expectedContent));
    }

    private Map<String, Object> getDownloadAuthenticationTokenHeader() {
        String authenticationToken = idamTestSupportUtil.getIdamTestUser();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", authenticationToken);
        return headers;
    }
}
