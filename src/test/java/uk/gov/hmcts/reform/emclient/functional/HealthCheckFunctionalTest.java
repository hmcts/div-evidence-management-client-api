package uk.gov.hmcts.reform.emclient.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication;

import com.jayway.jsonpath.JsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = EvidenceManagementClientApplication.class)
@PropertySource(value = "classpath:application.properties")
@TestPropertySource(properties = {"endpoints.health.time-to-live=0"})
public class HealthCheckFunctionalTest {

    @LocalServerPort
    private int port;

    @Value("${evidence.management.store.health.url}")
    private String evidenceManagementStoreApiUrl;

    @Value("${idam.s2s-auth.health.url}")
    private String serviceAuthApiUrl;


    @Autowired
    private RestTemplate restTemplate;

    private String healthUrl;
    private MockRestServiceServer mockRestServiceServer;
    private ClientHttpRequestFactory originalRequestFactory;
    private HttpClient httpClient = HttpClients.createMinimal();

    private HttpResponse getHealth() throws Exception {
        HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + String.valueOf(port) + "/health";
        originalRequestFactory = restTemplate.getRequestFactory();
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void tearDown() {
        restTemplate.setRequestFactory(originalRequestFactory);
    }

    @Test
    public void shouldReturnStatusUpWhenAllDependenciesAreUp() throws Exception {
        //stub stubEvidenceManagementStoreApiHealthUp
        stubHealthService(HttpStatus.OK, evidenceManagementStoreApiUrl, serviceAuthApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), "UP",
                "evidenceManagementStoreAPI", "serviceAuthProviderHealthCheck");

    }

    @Test
    public void shouldReturnStatusDownWhenAllDependenciesAreDown() throws Exception {

        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl,serviceAuthApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), "DOWN",
                "evidenceManagementStoreAPI", "serviceAuthProviderHealthCheck");
    }

    @Test
    public void shouldReturnStatusDownWhenEvidenceManagementStoreApiIsDown() throws Exception {
        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl,serviceAuthApiUrl);
        HttpResponse response = getHealth();
        assertStatus(EntityUtils.toString(getHealth().getEntity()), "DOWN", "evidenceManagementStoreAPI");
    }

    @Test
    public void shouldReturnStatusDownWhenServiceAuthApiIsDown() throws Exception {
        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl,serviceAuthApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), "DOWN", "serviceAuthProviderHealthCheck");
    }

    private void stubHealthService( HttpStatus healthStatus, String ... services) throws Exception {
        String resourceName = "/fixtures/evidence-management-store-api/healthcheck-down.json";
        if (healthStatus == HttpStatus.OK) {
            resourceName = "/fixtures/evidence-management-store-api/healthcheck-up.json";
        }
        for (String service: services) {
            stubHealthService(service, healthStatus, resourceName);
        }
    }

    private void assertStatus(String body, String checkStatus, String ... onServices) {
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo(checkStatus));
        for (String service : onServices) {
            assertThat(JsonPath.read(body, String.format("$.%s.status", service)).toString(), equalTo(checkStatus));
        }
        assertThat(JsonPath.read(body, "$.diskSpace.status").toString(), equalTo("UP"));
    }

    private void stubHealthService(String requestUrl, HttpStatus status, String resourceName) throws Exception {
        String responseBody = FileUtils.readFileToString(
                new File(getClass().getResource(resourceName).toURI()),
                Charset.defaultCharset());
        mockRestServiceServer.expect(once(), requestTo(requestUrl)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(status)
                        .body(responseBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}