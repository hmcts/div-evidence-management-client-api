package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.emclient.model.CreateUserRequest;
import uk.gov.hmcts.reform.divorce.emclient.model.UserCode;

import java.util.Base64;
import java.util.UUID;

@Service
public class IdamUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${auth.idam.client.secret}")
    private String idamSecret;

    private String idamUsername;

    private String idamPassword;

    private String testUserJwtToken;

    private String citizenUserAuthToken;

    String generateNewUserAndReturnToken() {
        if (citizenUserAuthToken == null) {
            String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
            String password = "genericPassword123";
            createUserInIdam(username, password);
            citizenUserAuthToken = generateUserTokenWithNoRoles(username, password);
        }
        return citizenUserAuthToken;
    }

    synchronized String getIdamTestUser() {
        if (StringUtils.isBlank(testUserJwtToken)) {
            createUserAndToken();
        }
        return testUserJwtToken;
    }

    private void createUserAndToken() {
        createUserInIdam();
        testUserJwtToken = generateUserTokenWithNoRoles(idamUsername, idamPassword);
    }

    private void createUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .email(username)
                .password(password)
                .forename("Test")
                .surname("User")
                .roles(new UserCode[] { UserCode.builder().code("citizen").build() })
                .userGroup(UserCode.builder().code("citizens").build())
                .build();

        Response createUserResponse = SerenityRest.given()
                .header("Content-Type", "application/json")
                .body(ResourceLoader.objectToJson(userRequest))
                .post(idamCreateUrl());

        throwExceptionOnErrorResponse(createUserResponse);
    }

    private void createUserInIdam() {
        idamUsername = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        idamPassword = "genericPassword123";

        createUserInIdam(idamUsername, idamPassword);
    }

    void createCaseworkerUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .email(username)
                .password(password)
                .forename("Test")
                .surname("User")
                .roles(new UserCode[] { UserCode.builder().code("caseworker-divorce-courtadmin").build() })
                .userGroup(UserCode.builder().code("caseworker").build())
                .build();

        SerenityRest.given()
                .header("Content-Type", "application/json")
                .body(ResourceLoader.objectToJson(userRequest))
                .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        Response response = SerenityRest.given()
                .header("Authorization", authHeader)
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());

        throwExceptionOnErrorResponse(response);

        response = SerenityRest.given()
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .relaxedHTTPSValidation()
                .post(idamTokenUrl(response.getBody().path("code")));

        throwExceptionOnErrorResponse(response);

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    private void throwExceptionOnErrorResponse(Response response) {
        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                    + " body: " + response.getBody().asString());
        }
    }

    private String idamCodeUrl() {
        return idamUserBaseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=divorce"
                + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {
        return idamUserBaseUrl + "/oauth2/token"
                + "?code=" + code
                + "&client_id=divorce"
                + "&client_secret=" + idamSecret
                + "&redirect_uri=" + idamRedirectUri
                + "&grant_type=authorization_code";
    }
}