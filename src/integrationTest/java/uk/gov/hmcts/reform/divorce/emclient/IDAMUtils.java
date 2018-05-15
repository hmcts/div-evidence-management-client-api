package uk.gov.hmcts.reform.divorce.emclient;

import com.nimbusds.jwt.JWTParser;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.springframework.beans.factory.annotation.Value;

import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import static jnr.posix.WString.path;

class IDAMUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    void createUserInIdam(String username, String password) {
        String s = "{\"email\":\"" + username + "@test.com\", \"forename\":\"" + username +
            "\",\"surname\":\"User\",\"password\":\"" + password + "\"}";

        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(s)
                .post(idamCreateUrl());
    }

    void createDivorceCaseworkerUserInIdam(String username, String password) {
        String body = "{\"email\":\"" + username + "@test.com" + "\", "
                + "\"forename\":" + "\"" + username + "\"," + "\"surname\":\"User\",\"password\":\"" + password + "\", "
                + "\"roles\":[\"caseworker-divorce\"], \"userGroup\":{\"code\":\"caseworker\"}}";

        System.out.println("David - user body"+body);
        System.out.println("David - IdamCreate"+idamCreateUrl());

        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String loginUrl() {
        return idamUserBaseUrl + "/oauth2/authorize?response_type=token&client_id=divorce&redirect_uri="
                            + "https://www.preprod.ccd.reform.hmcts.net/oauth2redirect";
    }

    String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username + "@test.com", password);
        System.out.println("David - userLoginDetails"+userLoginDetails);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        System.out.println("David - loginUrl()"+loginUrl());
        System.out.println("David - authHeader"+authHeader);


        ResponseBody authorization = RestAssured.given()
                .header("Authorization", authHeader)
                .post(loginUrl())
                .body();

        System.out.println("authenticationToken"+authorization.prettyPrint());
                String token  = authorization.path("access-token");

        System.out.println("David token>"+token);

        return "Bearer " + token;
    }

}