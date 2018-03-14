package uk.gov.hmcts.reform.emclient.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.client.Traverson.TraversalBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import uk.gov.hmcts.reform.emclient.exception.BinaryUrlNotAvailableException;

/**
 *
 * @author nitinprabhu
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceImplTest {
    private static final MediaType MEDIA_TYPE = new MediaType("application", "vnd.uk.gov.hmcts.dm.document.v1+json");

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Traverson mockTraverson;

    @Mock
    private TraversalBuilder traversalBuilder;

    @Mock
    private Link link;

    @InjectMocks
    private EvidenceManagementDownloadServiceImpl downloadService = spy(new EvidenceManagementDownloadServiceImpl());

    @Test
    public void shouldDownloadFileAndReturnInputStreamWhenValidInputsArePassed() throws Exception {
        mockRestTemplate();

        mockTraversionAPI("http://localhost:8080/documents/6/binary");

        assertThat(
                downloadService.downloadFile("http://localhost:8080/documents/6", "AAAABBBB", "12344").getStatusCode(),
                is(HttpStatus.OK));

        verifyInteractionsToDownloadFiles();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ResourceAccessException.class)
    public void shouldNotDownloadFileAndThrowExceptionWhenEMServiceIsUnavailable() throws Exception {
        when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
                Matchers.<HttpEntity<String>> any(), Matchers.<Class<Resource>> any()))
                        .thenThrow(ResourceAccessException.class);

        mockTraversionAPI("http://localhost:8080/documents/6/binary");

        downloadService.downloadFile("http://localhost:8080/documents/6", "AAAABBBB", "12344");

        verifyInteractionsToDownloadFiles();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BinaryUrlNotAvailableException.class)
    public void shouldNotDownloadFileAndThrowExceptionWhenBinaryUrlForResourceIsNotAvailable() throws Exception {
        when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
                Matchers.<HttpEntity<String>> any(), Matchers.<Class<Resource>> any()))
                        .thenThrow(ResourceAccessException.class);

        mockTraversionAPI(null);

        downloadService.downloadFile("http://localhost:8080/documents/6", "AAAABBBB", "12344");

        verifyInteractionsToDownloadFiles();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitialiseTraverson(){
        String uriText = "http:localhost/somePath";

        Traverson traverson = downloadService.initialiseTraverson(uriText);

        assertEquals(uriText, (ReflectionTestUtils.getField(traverson, "baseUri")).toString());

        assertTrue(((List<MediaType>)ReflectionTestUtils.getField(traverson, "mediaTypes")).contains(MEDIA_TYPE));
    }

    private void mockTraversionAPI(String binaryFileUrl) {
        doReturn(mockTraverson).when(downloadService).initialiseTraverson("http://localhost:8080/documents/6");

        when(mockTraverson.follow("$._links.binary.href")).thenReturn(traversalBuilder);

        when(traversalBuilder.asLink()).thenReturn(link);

        when(traversalBuilder.withHeaders(Matchers.<HttpHeaders> any())).thenReturn(traversalBuilder);

        when(link.getHref()).thenReturn(binaryFileUrl);
    }

    private void mockRestTemplate() {
        InputStreamResource marriageCertStream = new InputStreamResource(
                this.getClass().getResourceAsStream("/marriage-cert-example.png"));

        ResponseEntity<Resource> responseEntity = new ResponseEntity<Resource>(marriageCertStream, HttpStatus.OK);

        when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
                Matchers.<HttpEntity<?>> any(), Matchers.<Class<Resource>> any())).thenReturn(responseEntity);
    }

    private void verifyInteractionsToDownloadFiles() {
        verify(restTemplate).exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
                Matchers.<HttpEntity<?>> any(), Matchers.<Class<Resource>> any());

        verify(mockTraverson).follow("$._links.binary.href");

        verify(traversalBuilder).asLink();

        verify(traversalBuilder).withHeaders(Matchers.<HttpHeaders> any());

        verify(link).getHref();

        verifyNoMoreInteractions(restTemplate, mockTraverson, traversalBuilder, link);
    }

}
