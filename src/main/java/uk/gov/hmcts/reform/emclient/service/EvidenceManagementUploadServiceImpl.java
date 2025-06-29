package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static uk.gov.hmcts.reform.emclient.service.UploadRequestBuilder.param;

@Service
@Slf4j
public class EvidenceManagementUploadServiceImpl implements EvidenceManagementUploadService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Value("${evidence.management.store.upload.file.url}")
    private String evidenceManagementStoreUrl;

    @Qualifier("restTemplate")
    @Autowired
    private RestTemplate template;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private UserService userService;

    @Override
    public List<FileUploadResponse> upload(@NonNull final List<MultipartFile> files, final String authorizationToken,
                                           @Nullable String requestId) {
        UserDetails userDetails = userService.getUserDetails(authorizationToken);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param(files), headers(userDetails.getId()));
        JsonNode documents = template.postForObject(evidenceManagementStoreUrl, httpEntity, ObjectNode.class)
                .path("_embedded").path("documents");
        log.info("For Request Id {} and userId {} : File upload response from Evidence Management service is {}",
            requestId,
            userDetails.getId(),
            documents);

        return toUploadResponse(documents);
    }

    private FileUploadResponse createUploadResponse(JsonNode document) {
        return FileUploadResponse.builder()
                .status(HttpStatus.OK)
                .fileUrl(new HalLinkDiscoverer().findLinkWithRel("self",
                        document.toString()).getHref())
                .fileName(document.get("originalDocumentName").asText())
                .createdBy(getTextFromJsonNode(document, "createdBy"))
                .createdOn(document.get("createdOn").asText())
                .lastModifiedBy(getTextFromJsonNode(document, "lastModifiedBy"))
                .modifiedOn(getTextFromJsonNode(document, "modifiedOn"))
                .mimeType(document.get("mimeType").asText())
                .build();
    }

    private List<FileUploadResponse> toUploadResponse(JsonNode documents) {
        Stream<JsonNode> filesStream = stream(documents.spliterator(), false);
        return filesStream
                .map(this::createUploadResponse)
                .collect(Collectors.toList());
    }

    private String getTextFromJsonNode(JsonNode document, String attribute) {
        return Optional.ofNullable(document).flatMap(file -> Optional.ofNullable(attribute).map(file::asText))
                .orElse(null);
    }

    private HttpHeaders headers(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set("Content-Type", "multipart/form-data");
        headers.set("user-id", userId);
        return headers;
    }
}
