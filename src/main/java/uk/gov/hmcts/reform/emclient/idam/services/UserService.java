package uk.gov.hmcts.reform.emclient.idam.services;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;

@Component
public class UserService {

    private final IdamApiClient idamApiClient;

    private static final String BEARER = "Bearer";

    @Autowired
    public UserService(IdamApiClient idamApiClient) {
        this.idamApiClient = idamApiClient;
    }

    public UserDetails getUserDetails(String authorisation) {

        String authToken = StringUtils.containsIgnoreCase(authorisation, BEARER)
        ? authorisation
        : String.format("%s %s", BEARER, authorisation);
        return idamApiClient.retrieveUserDetails(authToken);
    }

}
