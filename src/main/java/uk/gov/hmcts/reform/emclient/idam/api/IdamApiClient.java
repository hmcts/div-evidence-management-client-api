package uk.gov.hmcts.reform.emclient.idam.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.emclient.idam.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApiClient {

    @GetMapping(value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);
}
