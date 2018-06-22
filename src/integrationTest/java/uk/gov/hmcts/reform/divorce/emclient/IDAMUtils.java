package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;

import java.util.Base64;
import java.util.List;

class IDAMUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.secret}")
    private String idamSecret;

    @Value("${auth.idam.redirect.url}")
    private String idamRedirectUrl;

    public IDAMUtils() {
        RestAssured.defaultParser = Parser.JSON;
    }

    public String getIdamTestUser(String username, String password) {
        createCitizen(username, password);
        return generateClientToken(username, password);
    }

    private void createCitizen(String username, String password) {
        RestAssured.given()
            .header("Content-Type", "application/json")
            .body("{\"email\":\"" + username + "\", \"forename\":\"Test\",\"surname\":\"User\",\"password\":\"" + password + "\"}")
            .post(idamCreateUrl());

    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String generateClientToken(String username, String password) {
        String code = generateClientCode(username, password);

        String token = RestAssured.given().post(idamUserBaseUrl + "/oauth2/token?code=" + code +
            "&client_secret=" + idamSecret +
            "&client_id=divorce" +
            "&redirect_uri=" + idamRedirectUrl +
            "&grant_type=authorization_code")
            .body().path("access_token");

        System.out.println("Generated token " + token);

        return "Bearer " + token;
    }

    private String generateClientCode(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        String code = RestAssured.given().baseUri(idamUserBaseUrl)
            .header("Authorization", "Basic " + encoded)
            .post("/oauth2/authorize?response_type=code&client_id=divorce&redirect_uri=" + idamRedirectUrl)
            .body().path("code");
        System.out.println("Generated code " + code);

        return code;
    }

    void createDivorceCaseworkerUserInIdam(String username, String password) {
        String body = "{\"email\":\"" + username + "@test.com" + "\", "
                + "\"forename\":" + "\"" + username + "\"," + "\"surname\":\"User\",\"password\":\"" + password + "\", "
                + "\"roles\":[\"caseworker-divorce\"], \"userGroup\":{\"code\":\"caseworker\"}}";
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .post(idamCreateUrl());
    }

}