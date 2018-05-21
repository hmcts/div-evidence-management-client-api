package uk.gov.hmcts.reform.emclient.idam.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;

@Component
public class UserService {

    private final IdamApiClient idamApiClient;

    @Autowired
    public UserService(IdamApiClient idamApiClient) {
        this.idamApiClient = idamApiClient;
    }

    public UserDetails getUserDetails(String authorisation) {
        String authToken = authorisation.contains("Bearer") ? authorisation : "Bearer "+authorisation;
        return idamApiClient.retrieveUserDetails(authToken);
    }

}
