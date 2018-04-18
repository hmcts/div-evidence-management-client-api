package uk.gov.hmcts.reform.emclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.JWTParser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.response.FileUploadResponse;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static uk.gov.hmcts.reform.emclient.service.UploadRequestBuilder.param;

@Service
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class EvidenceManagementUploadServiceImpl implements EvidenceManagementUploadService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Value("${evidence.management.store.upload.file.url}")
    private String evidenceManagementStoreUrl;

    @Autowired
    private RestTemplate template;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public List<FileUploadResponse> upload(@NonNull final List<MultipartFile> files, final String authorizationToken,
                                           @Nullable String requestId) {
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param(files), headers(authorizationToken));
        JsonNode documents = template.postForObject(evidenceManagementStoreUrl, httpEntity, ObjectNode.class)
                .path("_embedded").path("documents");
        log.info("For Request Id {} : File upload response from Evidence Management service is {}", requestId, documents);
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
        return Optional.ofNullable(document)
                .map(file -> Optional.ofNullable(attribute).map(file::asText).orElse(null))
                .orElse(null);
    }

    private HttpHeaders headers(String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set("Content-Type", "multipart/form-data");
        headers.set("user-id", getUserId(authorizationToken));
        return headers;
    }

    private String getUserId(String encodedJwt) {
        String userId = "divorceEmcli";
        Map<String, Object> claims;
        try {
            String jwt = encodedJwt.replaceFirst("Bearer ", "");
            claims = JWTParser.parse(jwt).getJWTClaimsSet().getClaims();
            userId = String.valueOf(claims.get("id"));
        } catch (Exception e) {
            log.error("failed parse user from jwt token [" + encodedJwt + "]", e);
        }
        return userId;
    }
}
