package uk.gov.hmcts.reform.divorce.emclient;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;

import java.net.URLEncoder;
import java.util.Base64;

import static io.restassured.config.EncoderConfig.encoderConfig;

class IDAMUtils {

    private final String idamUserBaseUrl;

    private final String idamSecret;

    private final String idamRedirectUrl;

    public IDAMUtils(String idamUserBaseUrl, String idamSecret, String idamRedirectUrl) {
        RestAssured.defaultParser = Parser.JSON;
        this.idamUserBaseUrl = idamUserBaseUrl;
        this.idamSecret = idamSecret;
        this.idamRedirectUrl = idamRedirectUrl;
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

        String token = RestAssured.given()
            .config(
                RestAssured
                    .config()
                    .encoderConfig(encoderConfig().encodeContentTypeAs("{\"mimeType\":\"application/x-www-form-urlencoded\",\"charset\":\"ISO-8859-1\"}", ContentType.URLENC)))
            .baseUri(idamUserBaseUrl)
            .header("Content-Type", ContentType.URLENC)
            .body("code=" + code +
                "&client_secret=" + idamSecret +
                "&client_id=divorce" +
                "&redirect_uri=" + idamRedirectUrl +
                "&grant_type=authorization_code")
            .post(idamUserBaseUrl + "/oauth2/token")
            .body().path("access_token");

        return "Bearer " + token;
    }

    private String generateClientCode(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        return RestAssured.given()
            .config(
                RestAssured
                    .config()
                    .encoderConfig(encoderConfig().encodeContentTypeAs("{\"mimeType\":\"application/x-www-form-urlencoded\",\"charset\":\"ISO-8859-1\"}", ContentType.URLENC)))
            .baseUri(idamUserBaseUrl)
            .header("Authorization", "Basic " + encoded)
            .header("Content-Type", ContentType.URLENC)
            .body(String.format("response_type=code&client_id=divorce&redirect_uri=%s", URLEncoder.encode(idamRedirectUrl)))
            .post("/oauth2/authorize")
            .body().path("code");
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