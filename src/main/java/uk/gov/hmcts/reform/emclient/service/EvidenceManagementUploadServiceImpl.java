package uk.gov.hmcts.reform.emclient.service;

import static java.util.stream.StreamSupport.stream;

import static uk.gov.hmcts.reform.emclient.service.UploadRequestBuilder.prepareRequest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class EvidenceManagementUploadServiceImpl implements EvidenceManagementUploadService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceManagementUploadServiceImpl.class);
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${evidence.management.upload.file.url}")
    private String evidenceManagementServiceURL;

    @Value("${evidence.management.store.upload.file.url}")
    private String evidenceManagementStoreUrl;

    @Autowired
    private RestTemplate template;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public List<FileUploadResponse> uploadFilesWithS2SAuthToken(List<MultipartFile> files, String authorizationToken, String requestId) {
        return uploadFiles(evidenceManagementStoreUrl, files, SERVICE_AUTHORIZATION_HEADER, authorizationToken, requestId);
    }

    @Override
    public List<FileUploadResponse> uploadFilesWithUserAuthToken(List<MultipartFile> files, String authorizationToken, String requestId) {
        return uploadFiles(evidenceManagementServiceURL, files, AUTHORIZATION_HEADER, authorizationToken, requestId);
    }

    private List<FileUploadResponse> uploadFiles(String uri, List<MultipartFile> files, String authHeaderName, String authorizationToken, String requestId) {
        MultiValueMap<String, Object> parameters = prepareRequest(files);

        HttpHeaders httpHeaders = setHttpHeaders(authHeaderName, authorizationToken);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parameters,
                httpHeaders);

        JsonNode filesJsonArray = template.postForObject(uri, httpEntity, ObjectNode.class)
                .get("_embedded")
                .get("documents");

        log.info("For Request Id {} : File upload response from Evidence Management service is {}", requestId,
                filesJsonArray);

        return prepareUploadResponse(filesJsonArray);
    }

    private List<FileUploadResponse> prepareUploadResponse(JsonNode filesJsonArray) {
        Stream<JsonNode> filesStream = stream(filesJsonArray.spliterator(), false);

        return filesStream
                .map(this::createUploadResponse)
                .collect(Collectors.toList());
    }

    private FileUploadResponse createUploadResponse(JsonNode storedFile) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse(HttpStatus.OK);
        fileUploadResponse.setFileUrl(new HalLinkDiscoverer().findLinkWithRel("self", storedFile.toString()).getHref());
        fileUploadResponse.setFileName(storedFile.get("originalDocumentName").asText());
        fileUploadResponse.setCreatedBy(getTextFromJsonNode(storedFile,"createdBy"));
        fileUploadResponse.setCreatedOn(storedFile.get("createdOn").asText());
        fileUploadResponse.setLastModifiedBy(getTextFromJsonNode(storedFile,"lastModifiedBy"));
        fileUploadResponse.setModifiedOn(getTextFromJsonNode(storedFile,"modifiedOn"));
        fileUploadResponse.setMimeType(storedFile.get("mimeType").asText());

        return fileUploadResponse;
    }

    private String getTextFromJsonNode(JsonNode storedFile, String node){
        if(storedFile == null || StringUtils.isBlank(node)){
            return null;
        }

        JsonNode jsonNode = storedFile.get(node);

        if(jsonNode == null){
            return null;
        }

        return jsonNode.asText();
    }

    private HttpHeaders setHttpHeaders(String authHeaderName, String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set("Content-Type", "multipart/form-data");
        headers.set("user-id", getUserId(authorizationToken));
        return headers;
    }

    public String getUserId(String encodedJwt) {
        String jwt = encodedJwt.replaceFirst("Bearer ", "");
        Map<String, Object> claims;
        try {
            claims = JWTParser.parse(jwt).getJWTClaimsSet().getClaims();

        } catch (ParseException e) {
            throw new IllegalStateException("Cannot find user from authorization token ", e);
        }
        return (String) claims.get("id");
    }
}
