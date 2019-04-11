package uk.gov.hmcts.reform.emclient.idam;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.emclient.idam.api.IdamApiClient;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;
import uk.gov.hmcts.reform.emclient.idam.services.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private IdamApiClient idamApiClient;

    @InjectMocks
    private UserService testObj;

    @Test
    public void userServiceReturnUserDetails(){

        UserDetails userDetails = UserDetails.builder().id("IdOne").build();
        String authToken = "Bearer authTokenValue";
        when(idamApiClient.retrieveUserDetails(authToken)).thenReturn(userDetails);

        assertThat(testObj.getUserDetails(authToken)).isEqualTo(userDetails);
    }

    @Test(expected = HttpClientErrorException.class)
    public void userServiceThrowErrorWhenNotAuthorised(){
        String authToken = "Bearer authTokenValue";
        when(idamApiClient.retrieveUserDetails(authToken)).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        testObj.getUserDetails(authToken);
    }
}