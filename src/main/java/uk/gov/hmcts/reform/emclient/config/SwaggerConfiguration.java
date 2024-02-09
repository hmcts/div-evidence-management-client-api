package uk.gov.hmcts.reform.emclient.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.emclient.controller.EvidenceManagementClientController;

@Configuration
@ConditionalOnProperty(name = "documentation.swagger.enabled", havingValue = "true")
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Value("${springdoc.swagger-ui.enabled}")
    private boolean swaggerEnabled;

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("api")
                .packagesToScan(EvidenceManagementClientController.class.getPackage().getName())
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
        if (swaggerEnabled) {
            registry.addResourceHandler("/swagger-ui.html**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                    .resourceChain(false);
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .resourceChain(false);
        }
    }
}