package uk.gov.hmcts.reform.emclient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

@Service
public class EvidenceManagementDownloadServiceImpl implements EvidenceManagementDownloadService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ROLE_HEADER = "user-roles";
    private static final String DIVORCE_CASEWORKER_ROLE = "caseworker-divorce-courtadmin";

    @Value("${evidence.management.upload.file.url}")
    private String evidenceManagementStoreUrl;

    @Autowired
    private RestTemplate template;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<byte[]> downloadFile(String fileId, String authorizationToken, String requestId) {
        userService.getUserDetails(authorizationToken);

        HttpHeaders headers = downloadHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        String url = evidenceManagementStoreUrl + "/"+ fileId+"/binary";

        return template.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
    }

    private HttpHeaders downloadHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set(USER_ROLE_HEADER, DIVORCE_CASEWORKER_ROLE);
        return headers;
    }
}
