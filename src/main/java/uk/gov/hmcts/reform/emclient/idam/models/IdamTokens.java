package uk.gov.hmcts.reform.emclient.idam.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IdamTokens {
    String idamOauth2Token;
    String serviceAuthorization;
    final String userId;
    final String email;
    final List<String> roles;
}
