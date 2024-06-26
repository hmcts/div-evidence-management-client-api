package uk.gov.hmcts.reform.emclient.functional;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = {EvidenceManagementClientApplication.class, HealthCheckFunctionalTest.LocalRibbonClientConfiguration.class})
@PropertySource(value = "classpath:application.properties")
@TestPropertySource(properties = {
    "endpoints.health.time-to-live=0",
    "feign.hystrix.enabled=true",
    "eureka.client.enabled=false"
    })
public class HealthCheckFunctionalTest extends BaseFunctionalTest {

    @LocalServerPort
    private int port;

    @Value("${evidence.management.store.health.url}")
    private String evidenceManagementStoreApiUrl;

    @ClassRule
    public static WireMockClassRule serviceAuthServer = new WireMockClassRule(4502);

    @Autowired
    @Qualifier("healthCheckRestTemplate")
    private RestTemplate restTemplate;

    private String healthUrl;
    private MockRestServiceServer mockRestServiceServer;
    private ClientHttpRequestFactory originalRequestFactory;
    private final HttpClient httpClient = HttpClients.createMinimal();
    private static final String UP = "UP";
    private static final String DOWN = "DOWN";
    private static final String EVIDENCE_MANAGEMENT_STORE_API = "evidenceManagementStoreAPI";
    private static final String HEALTHCHECK_UP_JSON = "/fixtures/evidence-management-store-api/healthcheck-up.json";
    private static final String HEALTHCHECK_DOWN_JSON = "/fixtures/evidence-management-store-api/healthcheck-down.json";

    private HttpResponse getHealth() throws Exception {
        HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + port + "/health";
        originalRequestFactory = restTemplate.getRequestFactory();
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void tearDown() {
        restTemplate.setRequestFactory(originalRequestFactory);
    }

    @Test
    public void shouldReturnStatusUpWhenAllDependenciesAreUp() throws Exception {
        mockServiceAuthFeignHealthCheck();
        stubHealthService(HttpStatus.OK, evidenceManagementStoreApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), UP,
                EVIDENCE_MANAGEMENT_STORE_API);
    }

    @Test
    public void shouldReturnStatusDownWhenAllDependenciesAreDown() throws Exception {
        mockServiceAuthFeignHealthCheck();
        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), DOWN,
                EVIDENCE_MANAGEMENT_STORE_API);
    }

    @Test
    public void shouldReturnStatusDownWhenEvidenceManagementStoreApiIsDown() throws Exception {
        mockServiceAuthFeignHealthCheck();
        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), UP, EVIDENCE_MANAGEMENT_STORE_API);
    }

    @Test
    public void shouldReturnStatusDownWhenServiceAuthApiIsDown() throws Exception {
        mockServiceAuthFeignHealthCheck();
        stubHealthService(HttpStatus.SERVICE_UNAVAILABLE, evidenceManagementStoreApiUrl);
        assertStatus(EntityUtils.toString(getHealth().getEntity()), DOWN);
    }

    private void stubHealthService(HttpStatus healthStatus, String...services) throws Exception {
        String resourceName = HEALTHCHECK_DOWN_JSON;
        if (healthStatus == HttpStatus.OK) {
            resourceName = HEALTHCHECK_UP_JSON;
        }
        for (String service: services) {
            stubHealthService(service, healthStatus, resourceName);
        }
    }

    private void stubHealthService(String requestUrl, HttpStatus status, String resourceName) throws Exception {
        String responseBody = FileUtils.readFileToString(
            new File(getClass().getResource(resourceName).toURI()),
            Charset.defaultCharset());
        mockRestServiceServer.expect(manyTimes(), requestTo(requestUrl)).andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(status)
                .body(responseBody)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void assertStatus(String body, String checkStatus, String...onServices) {
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo(checkStatus));
        for (String service : onServices) {
            assertThat(JsonPath.read(body, String.format("$.%s.status", service)).toString(), equalTo(checkStatus));
        }
        assertThat(JsonPath.read(body, "$.diskSpace.status").toString(), equalTo(UP));
    }

    private void mockServiceAuthFeignHealthCheck() throws URISyntaxException, IOException {
        String responseBody = FileUtils.readFileToString(
            new File(
                getClass().getResource(HEALTHCHECK_UP_JSON).toURI()),
            Charset.defaultCharset());

        serviceAuthServer.stubFor(get("/health")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseBody)));
    }
}
