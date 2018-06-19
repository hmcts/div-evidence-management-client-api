package uk.gov.hmcts.reform.emclient.functional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.emclient.application.EvidenceManagementClientApplication;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Lazy
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EvidenceManagementClientApplication.class)
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.properties")
@TestPropertySource(properties = {"endpoints.health.time-to-live=0",
        "service-auth-provider.service.stub.enabled=false",
        "evidence-management-api.service.stub.enabled=false"})
@AutoConfigureMockMvc
public class EvidenceManagementDeleteFunctionalTest extends BaseFunctionalTest{

    @Autowired
    private MockMvc webClient;

    private MockRestServiceServer mockRestServiceServer;
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String AUTHTOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZGFjaW5hbWh1dXV0ZHBoOGNqMWg0NGM4MSIsInN1YiI6IjE5IiwiaWF0IjoxNT" +
                "IyNzkxMDQ1LCJleHAiOjE1MjI3OTQ2NDUsImRhdGEiOiJjYXNld29ya2VyLWRpdm9yY2UsY2FzZXdvcmtlcixjYXNld29ya2V" +
                "yLWRpdm9yY2UtbG9hMSxjYXNld29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIxOSIsImZvcmVuYW1lIjoiQ2FzZV" +
                "dvcmtlclRlc3QiLCJzdXJuYW1lIjoiVXNlciIsImRlZmF1bHQtc2VydmljZSI6IkNDRCIsImxvYSI6MSwiZGVmYXVsdC11cmwiOi" +
                "JodHRwczovL2xvY2FsaG9zdDo5MDAwL3BvYy9jY2QiLCJncm91cCI6ImNhc2V3b3JrZXIifQ.y5tbI6Tg1bJLPkXm-nrI6D_FhM0pb" +
                "x72zDa1r7Qnp1M";

//   TODO - Clarify with Ganesh/Mathan @Value("${evidence.management.client.api.baseUrl}")
//    private String evidenceManagementClientApiBaseUrl;

    @Value("${evidence.management.client.api.service.port}")
    private String serverPort;

    @Autowired
    private RestTemplate restTemplate;
    private String docUri = "http://doc-store/1";
    private String API_URL = "/emclientapi/version/1/deleteFile?fileUrl=";

    @Before
    public void before() {
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void givenDocServiceReturnsForbiddenForBadS2SToken_thenReturn() throws Exception {

        mockDocumentService(HttpStatus.FORBIDDEN, docUri);

        MvcResult result = webClient.perform(delete(getAppBaseUrl(serverPort) + API_URL + docUri)
                .header(AUTHORIZATION_HEADER_NAME, AUTHTOKEN)
                .content(""))
                .andExpect(status().isForbidden())
                .andReturn();

        mockRestServiceServer.verify();
    }

    @Test
    public void givenFileUrlNotProvidedReturns405_thenReturn() throws Exception {

        mockDocumentService(HttpStatus.METHOD_NOT_ALLOWED, "");

        MvcResult result = webClient.perform(delete(getAppBaseUrl(serverPort) + API_URL)
                .header(AUTHORIZATION_HEADER_NAME, AUTHTOKEN)
                .content(""))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        mockRestServiceServer.verify();
    }

    @Test
    public void givenAllGoesWell_thenReturn() throws Exception {
        mockDocumentService(HttpStatus.NO_CONTENT, docUri);

        MvcResult result = webClient.perform(delete(getAppBaseUrl(serverPort)+ API_URL + docUri)
                .header(AUTHORIZATION_HEADER_NAME, AUTHTOKEN)
                .content(""))
                .andExpect(status().isNoContent())
                .andReturn();

        mockRestServiceServer.verify();
    }

    private void mockDocumentService(HttpStatus expectedResponse, String documentUrl) {
        mockRestServiceServer.expect(once(), requestTo(documentUrl)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(expectedResponse));
    }

    private static String authToken() {
        return "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZGFjaW5hbWh1dXV0ZHBoOGNqMWg0NGM4MSIsInN1YiI6IjE5IiwiaWF0IjoxNT" +
                "IyNzkxMDQ1LCJleHAiOjE1MjI3OTQ2NDUsImRhdGEiOiJjYXNld29ya2VyLWRpdm9yY2UsY2FzZXdvcmtlcixjYXNld29ya2V" +
                "yLWRpdm9yY2UtbG9hMSxjYXNld29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIxOSIsImZvcmVuYW1lIjoiQ2FzZV" +
                "dvcmtlclRlc3QiLCJzdXJuYW1lIjoiVXNlciIsImRlZmF1bHQtc2VydmljZSI6IkNDRCIsImxvYSI6MSwiZGVmYXVsdC11cmwiOi" +
                "JodHRwczovL2xvY2FsaG9zdDo5MDAwL3BvYy9jY2QiLCJncm91cCI6ImNhc2V3b3JrZXIifQ.y5tbI6Tg1bJLPkXm-nrI6D_FhM0pb" +
                "x72zDa1r7Qnp1M";
    }
}
