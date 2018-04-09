package uk.gov.hmcts.reform.emclient.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@SpringBootApplication
@ComponentScan(basePackages = "uk.gov.hmcts")
@EnableAutoConfiguration(exclude = HypermediaAutoConfiguration.class)
@EnableRetry(proxyTargetClass=true)
//@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class, FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@EnableCircuitBreaker
public class EvidenceManagementClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvidenceManagementClientApplication.class, args);
    }

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.auth.secret}") final String secret,
            @Value("${idam.auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

}
