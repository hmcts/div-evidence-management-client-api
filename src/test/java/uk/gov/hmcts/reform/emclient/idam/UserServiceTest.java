package uk.gov.hmcts.reform.emclient.idam;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private static final String AUTH_TOKEN = "Bearer authTokenValue";

    @Mock
    private IdamApiClient idamApiClient;

    @InjectMocks
    private UserService testObj;

    @Test
    public void userServiceReturnUserDetails(){

        UserDetails userDetails = UserDetails.builder().id("IdOne").build();
        when(idamApiClient.retrieveUserDetails(AUTH_TOKEN)).thenReturn(userDetails);

        assertThat(testObj.getUserDetails(AUTH_TOKEN)).isEqualTo(userDetails);
    }

    @Test(expected = HttpClientErrorException.class)
    public void userServiceThrowErrorWhenNotAuthorised(){
        when(idamApiClient.retrieveUserDetails(AUTH_TOKEN)).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        testObj.getUserDetails(AUTH_TOKEN);
    }
}