package uk.gov.hmcts.reform.emclient.idam;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.IdamTokens;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private IdamApiClient idamApiClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private UserService testObj;

    private static final String BEARER_AUTH_TOKEN = "Bearer authTokenValue";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Test
    public void userServiceReturnUserDetails() {

        UserDetails userDetails = UserDetails.builder().id("IdOne").build();
        String authToken = BEARER_AUTH_TOKEN;
        when(idamApiClient.retrieveUserDetails(authToken)).thenReturn(userDetails);

        assertThat(testObj.getUserDetails(authToken)).isEqualTo(userDetails);
    }

    @Test(expected = HttpClientErrorException.class)
    public void userServiceThrowErrorWhenNotAuthorised() {
        String authToken = BEARER_AUTH_TOKEN;
        when(idamApiClient.retrieveUserDetails(authToken)).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        testObj.getUserDetails(authToken);
    }

    @Test
    public void idamTokens() {
        List<String> roles = Arrays.asList("Role1");
        String idOne = "IdOne";
        String emailAddress = "emailAddress";
        UserDetails userDetails = UserDetails.builder().id(idOne)
            .email(emailAddress)
            .roles(roles)
            .build();
        when(idamApiClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        IdamTokens idamTokens = testObj.getIdamTokens(BEARER_AUTH_TOKEN);

        assertThat(idamTokens.getServiceAuthorization()).isEqualTo(SERVICE_AUTH_TOKEN);
        assertThat(idamTokens.getEmail()).isEqualTo(emailAddress);
        assertThat(idamTokens.getUserId()).isEqualTo(idOne);
        assertThat(idamTokens.getIdamOauth2Token()).isEqualTo(BEARER_AUTH_TOKEN);
        assertThat(idamTokens.getRoles()).isEqualTo(roles);
    }
}
