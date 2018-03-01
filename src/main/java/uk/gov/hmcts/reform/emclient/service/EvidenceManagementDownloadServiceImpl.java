package uk.gov.hmcts.reform.emclient.service;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.springframework.util.StreamUtils.copyToByteArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import uk.gov.hmcts.reform.emclient.exception.BinaryUrlNotAvailableException;

/**
 * 
 * @author nitinprabhu
 *
 */
@Service
public class EvidenceManagementDownloadServiceImpl implements EvidenceManagementDownloadService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceManagementDownloadServiceImpl.class);
    private static final MediaType MEDIA_TYPE = new MediaType("application", "vnd.uk.gov.hmcts.dm.document.v1+json");

    @Autowired
    private RestTemplate template;

    @Override
    public ResponseEntity<InputStreamResource> downloadFile(String fileUrl, String authorizationToken, String requestId)
            throws IOException {

        HttpHeaders httpHeaders = setHttpHeaders(authorizationToken);

        log.info("RequestId : {} and Downloading file for self url {} ", requestId, fileUrl);

        String binaryFileUrl = binaryUrl(fileUrl, httpHeaders);

        log.info("RequestId : {} and Binary file url retrieved is {} ", requestId, binaryFileUrl);

        if (isEmpty(binaryFileUrl)) {
            throw new BinaryUrlNotAvailableException("Binary url for that resource is not present");
        }

        ResponseEntity<Resource> resource = template.exchange(binaryFileUrl, HttpMethod.GET,
                new HttpEntity<String>(httpHeaders), Resource.class);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(resource.getHeaders())
                .body(new InputStreamResource(
                        new ByteArrayInputStream(copyToByteArray(resource.getBody().getInputStream()))));
    }

    public String binaryUrl(String url, HttpHeaders httpHeaders) {
        Traverson traverson = initialiseTraverson(url);

        return traverson
                .follow("$._links.binary.href")
                .withHeaders(httpHeaders)
                .asLink()
                .getHref();
    }

    Traverson initialiseTraverson(String url) {
        return new Traverson(URI.create(url), MEDIA_TYPE);
    }

    public HttpHeaders setHttpHeaders(String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authorizationToken);
        headers.set("Content-Type", MEDIA_TYPE.toString());
        return headers;
    }
}
