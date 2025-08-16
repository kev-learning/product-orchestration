package com.microservices.core.product.orchestration.service.config;

import com.microservices.core.util.api.APIDetailsConfiguration;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${api.common.version}:")
    private String version;

    @Value("${api.common.title}:")
    private String title;

    @Value("${api.common.description}:")
    private String description;

    @Autowired
    private APIDetailsConfiguration apiDetailsConfiguration;

    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .termsOfService(apiDetailsConfiguration.getTermsOfService())
                        .license(new License()
                                .name(apiDetailsConfiguration.getLicense())
                                .url(apiDetailsConfiguration.getLicenseUrl()))
                        .contact(new Contact()
                                .name(apiDetailsConfiguration.getContact().getName())
                                .email(apiDetailsConfiguration.getContact().getEmail())
                                .url(apiDetailsConfiguration.getContact().getUrl())))
                .externalDocs(new ExternalDocumentation()
                        .description(apiDetailsConfiguration.getExternalDocDesc())
                        .url(apiDetailsConfiguration.getExternalDocUrl()));

    }
}
