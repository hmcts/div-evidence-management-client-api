package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

@RunWith(SerenityRunner.class)
@Slf4j
public class EmClientFileDownloadTest extends IntegrationTest {

    private static final String FILE_TO_DOWNLOAD = "93e06406-453f-475e-b6e0-1a845221f44f";
    private static final String TEST_FILE = "documents/testDocument.pdf";

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration = new SpringIntegrationMethodRule();

    @Value("${evidence.management.client.api.baseUrl}")
    private String evidenceManagementClientApiBaseUrl;

    @Value("${feature.secure-doc-store}")
    private boolean secureDocStoreOn;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Test
    public void downloadFileTest() throws Exception {
        log.info("In the downloadFileTest with secureFlag {}", secureDocStoreOn);
        assumeFalse(secureDocStoreOn);
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
