package uk.gov.hmcts.reform.divorce.emclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class, FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class ServiceContextConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.auth.secret}") final String secret,
            @Value("${idam.auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    public IDAMUtils getIDAMUtil(
        @Value("${auth.idam.client.baseUrl}") final String baseUrl,
        @Value("${auth.idam.secret}") final String secret,
        @Value("${auth.idam.redirect.url}") final String redirectUrl) {
       return new IDAMUtils(baseUrl, secret, redirectUrl);
    }

    @Bean
    public EvidenceManagementTestUtils getEvidenceManagementTestUtils() {
        return new EvidenceManagementTestUtils();
    }
}
