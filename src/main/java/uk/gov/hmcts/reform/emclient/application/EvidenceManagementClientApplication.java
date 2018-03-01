package uk.gov.hmcts.reform.emclient.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 
 * @author nitinprabhu
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "uk.gov.hmcts")
@EnableAutoConfiguration(exclude = HypermediaAutoConfiguration.class)
@EnableRetry(proxyTargetClass=true)
@EnableCircuitBreaker
public class EvidenceManagementClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvidenceManagementClientApplication.class, args);
    }
}
