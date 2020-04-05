package uk.gov.hmcts.reform.divorce.emclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
@ImportAutoConfiguration({
    RibbonAutoConfiguration.class,
    HttpMessageConvertersAutoConfiguration.class,
    FeignRibbonClientAutoConfiguration.class,
    FeignAutoConfiguration.class,
    ServiceAuthAutoConfiguration.class})
public class ServiceContextConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.auth.secret}") final String secret,
            @Value("${idam.auth.microservice}") final String microService,
            @Autowired ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }
}