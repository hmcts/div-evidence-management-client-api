package uk.gov.hmcts.reform.emclient.utils;

import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EvidenceManagementServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(EvidenceManagementServiceUtils.class);

    public static String getUserId(String encodedJwt) {
        String userId = "divorceEmcli";
        Map<String, Object> claims;
        try {
            String jwt = encodedJwt.replaceFirst("Bearer ", "");
            claims = JWTParser.parse(jwt).getJWTClaimsSet().getClaims();
            userId = String.valueOf(claims.get("id"));
        } catch (Exception e) {
            log.error("failed parse user from jwt token [" + encodedJwt + "]", e);
        }
        return userId;
    }
}
