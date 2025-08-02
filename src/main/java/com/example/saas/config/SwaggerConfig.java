package com.example.saas.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI documentation for the application.  The
 * Springdoc starter automatically scans controller classes to generate
 * documentation; this bean customizes the top‑level metadata.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Multi‑Tenant SaaS API")
                        .description("REST API for a multi‑tenant SaaS platform with sharded PostgreSQL databases, Redis‑backed task queues and automatic auditing/soft deletion")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact().name("SaaS Support").email("support@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Repository")
                        .url("https://example.com/saas-app"));
    }
}