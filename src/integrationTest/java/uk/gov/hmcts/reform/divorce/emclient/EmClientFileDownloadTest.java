package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

@RunWith(SerenityRunner.class)
public class EmClientFileDownloadTest extends IntegrationTest {

    private static final String FILE_TO_DOWNLOAD = "93e06406-453f-475e-b6e0-1a845221f44f";
    private static final String TEST_FILE = "documents/testDocument.pdf";

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Test
    @DisabledIfSystemProperty(named = "feature.secure-doc-store", matches = "true")
    public void downloadFileTest() throws Exception {
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
