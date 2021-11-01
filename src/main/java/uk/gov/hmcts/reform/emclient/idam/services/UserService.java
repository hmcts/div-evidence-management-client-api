package uk.gov.hmcts.reform.emclient.idam.services;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;

@Component
public class UserService {

    private static final String BEARER = "Bearer";

    private final IdamApiClient idamApiClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public UserService(IdamApiClient idamApiClient, AuthTokenGenerator authTokenGenerator) {
        this.idamApiClient = idamApiClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    public UserDetails getUserDetails(String authorisation) {
        String authToken = StringUtils.containsIgnoreCase(authorisation, BEARER)
                ? authorisation
                : String.format("%s %s", BEARER, authorisation);
        return idamApiClient.retrieveUserDetails(authToken);
    }

    public IdamTokens getIdamTokens(String authorisation) {

        UserDetails userDetails = getUserDetails(authorisation);

        return IdamTokens.builder()
            .idamOauth2Token(authorisation)
            .serviceAuthorization(generateServiceAuthorization())
            .userId(userDetails.getId())
            .email(userDetails.getEmail())
            .roles(userDetails.getRoles())
            .build();
    }

    private String generateServiceAuthorization() {
        return authTokenGenerator.generate();
    }
}
